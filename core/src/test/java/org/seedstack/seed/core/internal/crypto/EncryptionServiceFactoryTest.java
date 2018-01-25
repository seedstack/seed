/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.crypto;

import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.HashMap;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.seed.crypto.EncryptionService;
import org.seedstack.shed.ClassLoaders;

/**
 * Unit test for {@link EncryptionServiceFactory}.
 */
@RunWith(JMockit.class)
public class EncryptionServiceFactoryTest {
    private static final String ALIAS = "key1";
    private static final char[] PASSWORD = "password".toCharArray();

    @Mocked
    private CryptoConfig configuration;
    @Mocked
    private KeyStore keyStore;
    @Mocked
    private Key key;
    @Mocked
    private Certificate certificate;
    @Mocked
    private PublicKey publicKey;

    @Test
    public void testCreateEncryptionService() throws Exception {
        new Expectations() {
            {
                keyStore.getKey(ALIAS, PASSWORD);
                result = key;
                keyStore.getCertificate(ALIAS);
                result = certificate;
                certificate.getPublicKey();
                result = publicKey;
            }
        };

        EncryptionServiceFactory factory = new EncryptionServiceFactory(configuration, keyStore);
        EncryptionService encryptionService = factory.create(ALIAS, PASSWORD);

        Assertions.assertThat(encryptionService).isNotNull();

        new Verifications() {
            {
                new EncryptionServiceImpl(ALIAS, publicKey, key);
            }
        };
    }

    @Test
    public void testCreateForAliasWithoutPrivateKey() throws KeyStoreException {
        new Expectations() {
            {
                keyStore.getCertificate(ALIAS);
                result = null;
            }
        };

        EncryptionServiceFactory factory = new EncryptionServiceFactory(configuration, keyStore);
        EncryptionService encryptionService = factory.create(ALIAS);

        Assertions.assertThat(encryptionService).isNotNull();

        new Verifications() {
            {
                new EncryptionServiceImpl(ALIAS, publicKey, null);
            }
        };
    }

    @Test(expected = SeedException.class)
    public void testCreateWithMissingAliasPassword() throws Exception {
        new Expectations() {
            {
                keyStore.getKey(ALIAS, null);
                result = new UnrecoverableKeyException();
            }
        };
        new EncryptionServiceFactory(configuration, keyStore).create(ALIAS, null);
    }

    @Test
    public void testCreateForAliasWithoutCertificate() throws Exception {
        new Expectations() {
            {
                keyStore.getKey(ALIAS, PASSWORD);
                result = key;
                keyStore.getCertificate(ALIAS);
                result = null;
            }
        };

        EncryptionServiceFactory factory = new EncryptionServiceFactory(configuration, keyStore);
        EncryptionService encryptionService = factory.create(ALIAS, PASSWORD);

        Assertions.assertThat(encryptionService).isNotNull();

        new Verifications() {
            {
                new EncryptionServiceImpl(ALIAS, publicKey, key);
            }
        };
    }

    @Test
    public void testCreateWithExternalCertificateFromResource(
            @Mocked CryptoConfig.CertificateConfig certificateConfig) throws Exception {
        final URL url = new URL("http://nowhere");

        new Expectations(url) {
            {
                url.getFile();
                result = null;
            }
        };

        new MockUp<ClassLoaders>() {
            @Mock
            public ClassLoader findMostCompleteClassLoader(Class<?> target) {
                return new ClassLoader() {
                    @Override
                    public URL getResource(String name) {
                        return url;
                    }
                };
            }
        };

        new Expectations() {
            {
                keyStore.getKey(ALIAS, PASSWORD);
                result = key;

                certificate.getPublicKey();
                result = publicKey;

                configuration.certificates();
                result = new HashMap<String, CryptoConfig.CertificateConfig>() {{
                    put(ALIAS, certificateConfig);
                }};

                certificateConfig.getResource();
                result = "path/to/cert";
            }
        };

        EncryptionServiceFactory factory = new EncryptionServiceFactory(configuration, keyStore);
        EncryptionService encryptionService = factory.create(ALIAS, PASSWORD);

        Assertions.assertThat(encryptionService).isNotNull();

        new Verifications() {
            {
                new EncryptionServiceImpl(ALIAS, publicKey, key);
            }
        };
    }

    @Test(expected = SeedException.class)
    public void testMissingCertificateFromResource(
            @Mocked CryptoConfig.CertificateConfig certificateConfig) {

        new Expectations() {
            {
                configuration.certificates();
                result = new HashMap<String, CryptoConfig.CertificateConfig>() {{
                    put(ALIAS, certificateConfig);
                }};

                certificateConfig.getResource();
                result = "path/to/cert";
            }
        };

        new EncryptionServiceFactory(configuration, keyStore).create(ALIAS, PASSWORD);
    }

}
