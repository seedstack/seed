/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import mockit.*;
import org.apache.commons.configuration.Configuration;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import javax.security.cert.X509Certificate;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class JCADefinitionFactoryTest {

    @Test
    public void testCreateKeyDefinitionWithFileCertificate(@Mocked final Configuration configuration, @Mocked final FileInputStream file,
                                                                   @Mocked final X509Certificate x509Certificate) throws Exception {

        final String alias = "alias";
        final String password = "password";

        new Expectations() {
            final String filename = "file.crt";

            {
                configuration.getString("cert.file");
                result = filename;
                configuration.getString("alias");
                result = alias;
                configuration.getString("password");
                result = password;

                new FileInputStream(filename);
                result = file;

                X509Certificate.getInstance(file);
                result = x509Certificate;

            }
        };
        JCADefinitionFactory factory = new JCADefinitionFactory();
        KeyDefinition definition = factory.createKeyDefinition(configuration, "test", null);

        Assertions.assertThat(definition.getAlias()).isEqualTo(alias);
        Assertions.assertThat(definition.getPassword()).isEqualTo(password);
        Assertions.assertThat(definition.getCertificate()).isEqualTo(x509Certificate);
    }

    @Test
    public void testCreateKeyDefinitionWithResourceCertificate(@Mocked final Configuration configuration, @Mocked final URL url,
                                                                       @Mocked final FileInputStream file, @Mocked final X509Certificate x509Certificate) throws Exception {

        final String alias = "alias";
        final String password = "password";
        final String filename = "client.ceree";

        new MockUp<ClassLoader>() {
            @Mock
            public URL getResource(Invocation inv, String name) {
                if (name.equals(filename)) {
                    return url;
                }
                return inv.proceed(name);
            }
        };
        new Expectations() {
            {
                configuration.getString("cert.resource");
                result = filename;
                configuration.getString("alias");
                result = alias;
                configuration.getString("password");
                result = password;

                url.getFile();
                result = filename;

                new FileInputStream(filename);
                result = file;

                X509Certificate.getInstance(file);
                result = x509Certificate;

            }
        };
        JCADefinitionFactory factory = new JCADefinitionFactory();
        KeyDefinition definition = factory.createKeyDefinition(configuration, "test", null);

        Assertions.assertThat(definition.getAlias()).isEqualTo(alias);
        Assertions.assertThat(definition.getPassword()).isEqualTo(password);
        Assertions.assertThat(definition.getCertificate()).isEqualTo(x509Certificate);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateKeyDefinitionWithResourceCertificateError(@Mocked final Configuration configuration) throws Exception {

        final String filename = "client.ceree";

        new Expectations() {
            {
                configuration.getString("cert.resource");
                result = filename;

            }
        };
        JCADefinitionFactory factory = new JCADefinitionFactory();
        factory.createKeyDefinition(configuration, "test", null);

    }

    @Test
    public void testCreateKeyDefinitionWithoutCertificate(@Mocked final Configuration configuration) throws Exception {

        final String alias = "alias";
        final String password = "password";

        new Expectations() {
            {
                configuration.getString("alias");
                result = alias;
                configuration.getString("password");
                result = password;
            }
        };
        JCADefinitionFactory factory = new JCADefinitionFactory();
        KeyDefinition definition = factory.createKeyDefinition(configuration, "test", null);

        Assertions.assertThat(definition.getAlias()).isEqualTo(alias);
        Assertions.assertThat(definition.getPassword()).isEqualTo(password);
        Assertions.assertThat(definition.getCertificate()).isNull();
    }

    @Test(expected = RuntimeException.class)
    public void testCreateKeyDefinitionWithFileNotFoundExceptionCertificate(@Mocked final Configuration configuration,
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
        JCADefinitionFactory factory = new JCADefinitionFactory();
        factory.createKeyDefinition(configuration, "test", null);

    }

    @Test(expected = RuntimeException.class)
    public void testCreateKeyDefinitionWithCertificateException(@Mocked final Configuration configuration, @Mocked final FileInputStream file,
                                                                        @SuppressWarnings("unused") @Mocked final X509Certificate x509Certificate) throws Exception {

        new Expectations() {
            final String filename = "file.crt";

            {
                configuration.getString("cert.file");
                result = filename;

                new FileInputStream(filename);
                result = file;

                X509Certificate.getInstance(file);
                result = new javax.security.cert.CertificateException("dummy exception");

            }
        };
        JCADefinitionFactory factory = new JCADefinitionFactory();
        factory.createKeyDefinition(configuration, "test", null);

    }

    @Test(expected = RuntimeException.class)
    public void testCreateKeyDefinitionWithIOException(@Mocked final Configuration configuration, @Mocked final FileInputStream file,
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
        JCADefinitionFactory factory = new JCADefinitionFactory();
        factory.createKeyDefinition(configuration, "test", null);

    }
}
