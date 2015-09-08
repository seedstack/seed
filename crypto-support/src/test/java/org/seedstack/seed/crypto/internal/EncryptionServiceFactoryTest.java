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
import mockit.Mocked;
import mockit.Verifications;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(KeyDefinition)}
     * .
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testCreateEncryptionService(@Mocked final KeyStoreDefinition keyStoreDefinition, @Mocked final KeyDefinition keyDefinition,
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
        factory.createEncryptionService(keyDefinition);

        new Verifications() {
            {
                new EncryptionServiceImpl(keyStore, keyDefinition);
                times = 1;
            }
        };
    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(KeyDefinition)}
     * . Test only a public key (no keystore needed).
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testCreateEncryptionServiceWithoutKeystore(@Mocked final KeyStoreDefinition keyStoreDefinition,
                                                           @Mocked final KeyDefinition keyDefinition,
                                                           @SuppressWarnings("unused") @Mocked final EncryptionServiceImpl asymetricCrypting) throws Exception {
        new Expectations() {
            {
                keyStoreDefinition.getPath();
                returns(null);

            }
        };

        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        factory.createEncryptionService(keyDefinition);

        new Verifications() {
            {
                new EncryptionServiceImpl(null, keyDefinition);
                times = 1;
            }
        };
    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(KeyDefinition)}
     * . Test only a public key (no keystore needed).
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testCreateEncryptionServiceWithoutKeystoreDefinition(@Mocked final KeyDefinition keyDefinition,
                                                                     @SuppressWarnings("unused") @Mocked final EncryptionServiceImpl asymetricCrypting) throws Exception {

        EncryptionServiceFactory factory = new EncryptionServiceFactory();
        factory.createEncryptionService(keyDefinition);

        new Verifications() {
            {
                new EncryptionServiceImpl(null, keyDefinition);
                times = 1;
            }
        };
    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(KeyDefinition)}
     * . Test a {@link KeyStoreException} if no Provider supports a KeyStoreSpi implementation for the specified type.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testCreateEncryptionServiceWithKeystoreException(@Mocked final KeyStoreDefinition keyStoreDefinition,
                                                                 @Mocked final KeyDefinition keyDefinition, @SuppressWarnings("unused") @Mocked final KeyStore keyStore) throws Exception {
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
        factory.createEncryptionService(keyDefinition);

    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(KeyDefinition)}
     * . Test a {@link FileNotFoundException} if no keystore found.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testCreateEncryptionServiceWithNoKeystoreFound(@Mocked final KeyStoreDefinition keyStoreDefinition,
                                                               @Mocked final KeyDefinition keyDefinition, @Mocked final KeyStore keyStore,
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
        factory.createEncryptionService(keyDefinition);

    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(KeyDefinition)}
     * . Test a {@link NoSuchAlgorithmException} if keystore can not be loaded.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testCreateEncryptionServiceWithKeystoreAlgorithmException(@Mocked final KeyStoreDefinition keyStoreDefinition,
                                                                          @Mocked final KeyDefinition keyDefinition, @Mocked final KeyStore keyStore, @Mocked final FileInputStream file,
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
        factory.createEncryptionService(keyDefinition);

    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(KeyDefinition)}
     * . Test a {@link CertificateException} if keystore can not be loaded.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testCreateEncryptionServiceWithKeystoreCertificateException(@Mocked final KeyStoreDefinition keyStoreDefinition,
                                                                            @Mocked final KeyDefinition keyDefinition, @Mocked final KeyStore keyStore, @Mocked final FileInputStream file,
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
        factory.createEncryptionService(keyDefinition);

    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(KeyDefinition)}
     * . Test a bad password to load the keystore.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testCreateEncryptionServiceWithKeystoreIncorrectPassword(@Mocked final KeyStoreDefinition keyStoreDefinition,
                                                                         @Mocked final KeyDefinition keyDefinition, @Mocked final KeyStore keyStore, @Mocked final FileInputStream file,
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
        factory.createEncryptionService(keyDefinition);

    }

    /**
     * Test method for
     * {@link org.seedstack.seed.crypto.internal.EncryptionServiceFactory#createEncryptionService(KeyDefinition)}
     * . Test a bad password to load the keystore.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testCreateEncryptionServiceWithCloseError(@Mocked final KeyStoreDefinition keyStoreDefinition,
                                                          @Mocked final KeyDefinition keyDefinition, @Mocked final KeyStore keyStore, @Mocked final FileInputStream file,
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
        factory.createEncryptionService(keyDefinition);

    }

}
