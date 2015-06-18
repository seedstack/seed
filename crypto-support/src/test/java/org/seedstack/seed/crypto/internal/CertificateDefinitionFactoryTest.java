/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 10 juin 2015
 */
/**
 * 
 */
package org.seedstack.seed.crypto.internal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import mockit.Expectations;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

import org.apache.commons.configuration.Configuration;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Unit test for {@link CertificateDefinitionFactory}. Check {@link CertificateDefinition} creation.
 * 
 * @author thierry.bouvet@mpsa.com
 */
public class CertificateDefinitionFactoryTest {

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.CertificateDefinitionFactory#getInstance(org.apache.commons.configuration.Configuration)}.
     * 
     * @throws Exception if an error occurred
     */
    @Test
    public void testGetInstanceWithFileCertificate(@Mocked final Configuration configuration, @Mocked final FileInputStream file,
            @Mocked final X509Certificate x509Certificate) throws Exception {

        final String alias = "alias";
        final String password = "password";

        new Expectations() {
            final String filename = "file.crt";
            {
                configuration.getString("cert.file");
                result = filename;
                configuration.getString("keystore.alias");
                result = alias;
                configuration.getString("key.password");
                result = password;

                new FileInputStream(filename);
                result = file;

                X509Certificate.getInstance(file);
                result = x509Certificate;

            }
        };
        CertificateDefinitionFactory factory = new CertificateDefinitionFactory();
        CertificateDefinition definition = factory.getInstance(configuration);

        Assertions.assertThat(definition.getAlias()).isEqualTo(alias);
        Assertions.assertThat(definition.getPassword()).isEqualTo(password);
        Assertions.assertThat(definition.getCertificate()).isEqualTo(x509Certificate);
    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.CertificateDefinitionFactory#getInstance(org.apache.commons.configuration.Configuration)}.
     * 
     * @throws Exception if an error occurred
     */
    @Test
    public void testGetInstanceWithResourceCertificate(@Mocked final Configuration configuration, @Mocked final URL url,
            @Mocked final FileInputStream file, @Mocked final X509Certificate x509Certificate) throws Exception {

        final String alias = "alias";
        final String password = "password";
        final String filename = "client.ceree";

        new MockUp<ClassLoader>() {
            @Mock
            public URL getResource(Invocation inv, String name) {
                if (name == filename) {
                    return url;
                }
                return inv.proceed(name);
            }
        };
        new Expectations() {
            {
                configuration.getString("cert.resource");
                result = filename;
                configuration.getString("keystore.alias");
                result = alias;
                configuration.getString("key.password");
                result = password;

                url.getFile();
                result = filename;

                new FileInputStream(filename);
                result = file;

                X509Certificate.getInstance(file);
                result = x509Certificate;

            }
        };
        CertificateDefinitionFactory factory = new CertificateDefinitionFactory();
        CertificateDefinition definition = factory.getInstance(configuration);

        Assertions.assertThat(definition.getAlias()).isEqualTo(alias);
        Assertions.assertThat(definition.getPassword()).isEqualTo(password);
        Assertions.assertThat(definition.getCertificate()).isEqualTo(x509Certificate);
    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.CertificateDefinitionFactory#getInstance(org.apache.commons.configuration.Configuration)}.
     * 
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testGetInstanceWithResourceCertificateError(@Mocked final Configuration configuration) throws Exception {

        final String filename = "client.ceree";

        new Expectations() {
            {
                configuration.getString("cert.resource");
                result = filename;

            }
        };
        CertificateDefinitionFactory factory = new CertificateDefinitionFactory();
        factory.getInstance(configuration);

    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.CertificateDefinitionFactory#getInstance(org.apache.commons.configuration.Configuration)}.
     * 
     * @throws Exception if an error occurred
     */
    @Test
    public void testGetInstanceWithoutCertificate(@Mocked final Configuration configuration) throws Exception {

        final String alias = "alias";
        final String password = "password";

        new Expectations() {
            {
                configuration.getString("keystore.alias");
                result = alias;
                configuration.getString("key.password");
                result = password;
            }
        };
        CertificateDefinitionFactory factory = new CertificateDefinitionFactory();
        CertificateDefinition definition = factory.getInstance(configuration);

        Assertions.assertThat(definition.getAlias()).isEqualTo(alias);
        Assertions.assertThat(definition.getPassword()).isEqualTo(password);
        Assertions.assertThat(definition.getCertificate()).isNull();
    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.CertificateDefinitionFactory#getInstance(org.apache.commons.configuration.Configuration)}.
     * 
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testGetInstanceWithFileNotFoundExceptionCertificate(@Mocked final Configuration configuration,
            @SuppressWarnings("unused") @Mocked final FileInputStream file) throws Exception {

        new Expectations() {
            final String filename = "file.crt";
            {
                configuration.getString("cert.file");
                result = filename;

                new FileInputStream(filename);
                result = new FileNotFoundException("dummy exception");

            }
        };
        CertificateDefinitionFactory factory = new CertificateDefinitionFactory();
        factory.getInstance(configuration);

    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.CertificateDefinitionFactory#getInstance(org.apache.commons.configuration.Configuration)}.
     * 
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testGetInstanceWithCertificateException(@Mocked final Configuration configuration, @Mocked final FileInputStream file,
            @SuppressWarnings("unused") @Mocked final X509Certificate x509Certificate) throws Exception {

        new Expectations() {
            final String filename = "file.crt";
            {
                configuration.getString("cert.file");
                result = filename;

                new FileInputStream(filename);
                result = file;

                X509Certificate.getInstance(file);
                result = new CertificateException("dummy exception");

            }
        };
        CertificateDefinitionFactory factory = new CertificateDefinitionFactory();
        factory.getInstance(configuration);

    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.CertificateDefinitionFactory#getInstance(org.apache.commons.configuration.Configuration)}.
     * 
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testGetInstanceWithIOException(@Mocked final Configuration configuration, @Mocked final FileInputStream file,
            @Mocked final X509Certificate x509Certificate) throws Exception {

        new Expectations() {
            final String filename = "file.crt";
            {
                configuration.getString("cert.file");
                result = filename;

                new FileInputStream(filename);
                result = file;

                X509Certificate.getInstance(file);
                result = x509Certificate;

                file.close();
                result = new IOException("dummy exception");
            }
        };
        CertificateDefinitionFactory factory = new CertificateDefinitionFactory();
        factory.getInstance(configuration);

    }

}
