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

import mockit.Expectations;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Verifications;
import org.apache.commons.configuration.Configuration;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import javax.security.cert.X509Certificate;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Unit test for {@link EncryptionServiceFactory}.
 *
 * @author thierry.bouvet@mpsa.com
 */
public class EncryptionServiceFactoryTest {

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(org.seedstack.seed.crypto.internal.KeyStoreDefinition, org.seedstack.seed.crypto.internal.CertificateDefinition)}
     * .
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testCreateEncryptionService(@Mocked final KeyStoreDefinition keyStoreDefinition, @Mocked final CertificateDefinition certificateDefinition,
                                            @Mocked final KeyStore keyStore, @Mocked final FileInputStream file,
                                            @SuppressWarnings("unused") @Mocked final EncryptionServiceImpl asymetricCrypting) throws Exception {
        new Expectations() {
            final String pathToKeystore = "pathToKeystore";

            {
                keyStoreDefinition.getPath();
                returns(pathToKeystore);

                KeyStore.getInstance(KeyStore.getDefaultType());
                returns(keyStore);

                new FileInputStream(pathToKeystore);
                returns(file);

                keyStoreDefinition.getPassword();
                returns("password");
            }
        };

        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        factory.createEncryptionService(keyStoreDefinition, certificateDefinition);

        new Verifications() {
            {
                new EncryptionServiceImpl(keyStore, certificateDefinition);
                times = 1;
            }
        };
    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(org.seedstack.seed.crypto.internal.KeyStoreDefinition, org.seedstack.seed.crypto.internal.CertificateDefinition)}
     * . Test only a public key (no keystore needed).
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testCreateEncryptionServiceWithoutKeystore(@Mocked final KeyStoreDefinition keyStoreDefinition,
                                                           @Mocked final CertificateDefinition certificateDefinition,
                                                           @SuppressWarnings("unused") @Mocked final EncryptionServiceImpl asymetricCrypting) throws Exception {
        new Expectations() {
            {
                keyStoreDefinition.getPath();
                returns(null);

            }
        };

        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        factory.createEncryptionService(keyStoreDefinition, certificateDefinition);

        new Verifications() {
            {
                new EncryptionServiceImpl(null, certificateDefinition);
                times = 1;
            }
        };
    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(org.seedstack.seed.crypto.internal.KeyStoreDefinition, org.seedstack.seed.crypto.internal.CertificateDefinition)}
     * . Test only a public key (no keystore needed).
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testCreateEncryptionServiceWithoutKeystoreDefinition(@Mocked final CertificateDefinition certificateDefinition,
                                                                     @SuppressWarnings("unused") @Mocked final EncryptionServiceImpl asymetricCrypting) throws Exception {

        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        factory.createEncryptionService(null, certificateDefinition);

        new Verifications() {
            {
                new EncryptionServiceImpl(null, certificateDefinition);
                times = 1;
            }
        };
    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(org.seedstack.seed.crypto.internal.KeyStoreDefinition, org.seedstack.seed.crypto.internal.CertificateDefinition)}
     * . Test a {@link KeyStoreException} if no Provider supports a KeyStoreSpi implementation for the specified type.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testCreateEncryptionServiceWithKeystoreException(@Mocked final KeyStoreDefinition keyStoreDefinition,
                                                                 @Mocked final CertificateDefinition certificateDefinition, @SuppressWarnings("unused") @Mocked final KeyStore keyStore) throws Exception {
        new Expectations() {
            final String pathToKeystore = "pathToKeystore";

            {
                keyStoreDefinition.getPath();
                returns(pathToKeystore);

                KeyStore.getInstance(KeyStore.getDefaultType());
                result = new KeyStoreException("dummy exception");
            }
        };

        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        factory.createEncryptionService(keyStoreDefinition, certificateDefinition);

    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(org.seedstack.seed.crypto.internal.KeyStoreDefinition, org.seedstack.seed.crypto.internal.CertificateDefinition)}
     * . Test a {@link FileNotFoundException} if no keystore found.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testCreateEncryptionServiceWithNoKeystoreFound(@Mocked final KeyStoreDefinition keyStoreDefinition,
                                                               @Mocked final CertificateDefinition certificateDefinition, @Mocked final KeyStore keyStore,
                                                               @SuppressWarnings("unused") @Mocked final FileInputStream file) throws Exception {
        new Expectations() {
            final String pathToKeystore = "pathToKeystore";

            {
                keyStoreDefinition.getPath();
                returns(pathToKeystore);

                KeyStore.getInstance(KeyStore.getDefaultType());
                returns(keyStore);

                new FileInputStream(pathToKeystore);
                result = new FileNotFoundException("dummy exception");
            }
        };

        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        factory.createEncryptionService(keyStoreDefinition, certificateDefinition);

    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(org.seedstack.seed.crypto.internal.KeyStoreDefinition, org.seedstack.seed.crypto.internal.CertificateDefinition)}
     * . Test a {@link NoSuchAlgorithmException} if keystore can not be loaded.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testCreateEncryptionServiceWithKeystoreAlgorithmException(@Mocked final KeyStoreDefinition keyStoreDefinition,
                                                                          @Mocked final CertificateDefinition certificateDefinition, @Mocked final KeyStore keyStore, @Mocked final FileInputStream file,
                                                                          @SuppressWarnings("unused") @Mocked final EncryptionServiceImpl asymetricCrypting) throws Exception {
        new Expectations() {
            final String pathToKeystore = "pathToKeystore";
            final String password = "password";

            {
                keyStoreDefinition.getPath();
                returns(pathToKeystore);

                KeyStore.getInstance(KeyStore.getDefaultType());
                returns(keyStore);

                new FileInputStream(pathToKeystore);
                result = file;

                keyStoreDefinition.getPassword();
                returns(password);

                keyStore.load(file, password.toCharArray());
                result = new NoSuchAlgorithmException("dummy exception");
            }
        };

        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        factory.createEncryptionService(keyStoreDefinition, certificateDefinition);

    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(org.seedstack.seed.crypto.internal.KeyStoreDefinition, org.seedstack.seed.crypto.internal.CertificateDefinition)}
     * . Test a {@link CertificateException} if keystore can not be loaded.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testCreateEncryptionServiceWithKeystoreCertificateException(@Mocked final KeyStoreDefinition keyStoreDefinition,
                                                                            @Mocked final CertificateDefinition certificateDefinition, @Mocked final KeyStore keyStore, @Mocked final FileInputStream file,
                                                                            @SuppressWarnings("unused") @Mocked final EncryptionServiceImpl asymetricCrypting) throws Exception {
        new Expectations() {
            final String pathToKeystore = "pathToKeystore";
            final String password = "password";

            {
                keyStoreDefinition.getPath();
                returns(pathToKeystore);

                KeyStore.getInstance(KeyStore.getDefaultType());
                returns(keyStore);

                new FileInputStream(pathToKeystore);
                result = file;

                keyStoreDefinition.getPassword();
                returns(password);

                keyStore.load(file, password.toCharArray());
                result = new CertificateException("dummy exception");
            }
        };

        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        factory.createEncryptionService(keyStoreDefinition, certificateDefinition);

    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(org.seedstack.seed.crypto.internal.KeyStoreDefinition, org.seedstack.seed.crypto.internal.CertificateDefinition)}
     * . Test a bad password to load the keystore.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testCreateEncryptionServiceWithKeystoreIncorrectPassword(@Mocked final KeyStoreDefinition keyStoreDefinition,
                                                                         @Mocked final CertificateDefinition certificateDefinition, @Mocked final KeyStore keyStore, @Mocked final FileInputStream file,
                                                                         @SuppressWarnings("unused") @Mocked final EncryptionServiceImpl asymetricCrypting) throws Exception {
        new Expectations() {
            final String pathToKeystore = "pathToKeystore";
            final String password = "password";

            {
                keyStoreDefinition.getPath();
                returns(pathToKeystore);

                KeyStore.getInstance(KeyStore.getDefaultType());
                returns(keyStore);

                new FileInputStream(pathToKeystore);
                result = file;

                keyStoreDefinition.getPassword();
                returns(password);

                keyStore.load(file, password.toCharArray());
                result = new IOException("dummy exception");
            }
        };

        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        factory.createEncryptionService(keyStoreDefinition, certificateDefinition);

    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(org.seedstack.seed.crypto.internal.KeyStoreDefinition, org.seedstack.seed.crypto.internal.CertificateDefinition)}
     * . Test a bad password to load the keystore.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testCreateEncryptionServiceWithCloseError(@Mocked final KeyStoreDefinition keyStoreDefinition,
                                                          @Mocked final CertificateDefinition certificateDefinition, @Mocked final KeyStore keyStore, @Mocked final FileInputStream file,
                                                          @SuppressWarnings("unused") @Mocked final EncryptionServiceImpl asymetricCrypting) throws Exception {
        new Expectations() {
            final String pathToKeystore = "pathToKeystore";
            final String password = "password";

            {
                keyStoreDefinition.getPath();
                returns(pathToKeystore);

                KeyStore.getInstance(KeyStore.getDefaultType());
                returns(keyStore);

                new FileInputStream(pathToKeystore);
                result = file;

                keyStoreDefinition.getPassword();
                returns(password);

                file.close();
                result = new IOException("dummy exception");
            }
        };

        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        factory.createEncryptionService(keyStoreDefinition, certificateDefinition);

    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createCertificateDefinition(Configuration, String)}
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testCreateCertificateDefinitionWithFileCertificate(@Mocked final Configuration configuration, @Mocked final FileInputStream file,
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
        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        CertificateDefinition definition = factory.createCertificateDefinition(configuration, "test");

        Assertions.assertThat(definition.getAlias()).isEqualTo(alias);
        Assertions.assertThat(definition.getPassword()).isEqualTo(password);
        Assertions.assertThat(definition.getCertificate()).isEqualTo(x509Certificate);
    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createCertificateDefinition(Configuration, String)}
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testCreateCertificateDefinitionWithResourceCertificate(@Mocked final Configuration configuration, @Mocked final URL url,
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
        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        CertificateDefinition definition = factory.createCertificateDefinition(configuration, "test");

        Assertions.assertThat(definition.getAlias()).isEqualTo(alias);
        Assertions.assertThat(definition.getPassword()).isEqualTo(password);
        Assertions.assertThat(definition.getCertificate()).isEqualTo(x509Certificate);
    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createCertificateDefinition(Configuration, String)}
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testCreateCertificateDefinitionWithResourceCertificateError(@Mocked final Configuration configuration) throws Exception {

        final String filename = "client.ceree";

        new Expectations() {
            {
                configuration.getString("cert.resource");
                result = filename;

            }
        };
        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        factory.createCertificateDefinition(configuration, "test");

    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createCertificateDefinition(Configuration, String)}
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testCreateCertificateDefinitionWithoutCertificate(@Mocked final Configuration configuration) throws Exception {

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
        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        CertificateDefinition definition = factory.createCertificateDefinition(configuration, "test");

        Assertions.assertThat(definition.getAlias()).isEqualTo(alias);
        Assertions.assertThat(definition.getPassword()).isEqualTo(password);
        Assertions.assertThat(definition.getCertificate()).isNull();
    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createCertificateDefinition(Configuration, String)}
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testCreateCertificateDefinitionWithFileNotFoundExceptionCertificate(@Mocked final Configuration configuration,
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
        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        factory.createCertificateDefinition(configuration, "test");

    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createCertificateDefinition(Configuration, String)}
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testCreateCertificateDefinitionWithCertificateException(@Mocked final Configuration configuration, @Mocked final FileInputStream file,
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
        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        factory.createCertificateDefinition(configuration, "test");

    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createCertificateDefinition(Configuration, String)}
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testCreateCertificateDefinitionWithIOException(@Mocked final Configuration configuration, @Mocked final FileInputStream file,
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
        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        factory.createCertificateDefinition(configuration, "test");

    }


}
