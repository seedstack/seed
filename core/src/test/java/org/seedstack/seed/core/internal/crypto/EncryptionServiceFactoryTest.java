/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.crypto;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.seed.crypto.EncryptionService;

/**
 * Unit test for {@link EncryptionServiceFactory}.
 */
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

        EncryptionServiceFactory factory = new EncryptionServiceFactory(keyStore);
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

        EncryptionServiceFactory factory = new EncryptionServiceFactory(keyStore);
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
        new EncryptionServiceFactory(keyStore).create(ALIAS, null);
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

        EncryptionServiceFactory factory = new EncryptionServiceFactory(keyStore);
        EncryptionService encryptionService = factory.create(ALIAS, PASSWORD);

        Assertions.assertThat(encryptionService).isNotNull();

        new Verifications() {
            {
                new EncryptionServiceImpl(ALIAS, publicKey, key);
            }
        };
    }
}
