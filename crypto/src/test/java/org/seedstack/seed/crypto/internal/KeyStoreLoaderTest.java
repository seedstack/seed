/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import mockit.Expectations;
import mockit.Mocked;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.core.api.SeedException;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.security.KeyStore;

import static org.junit.Assert.fail;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class KeyStoreLoaderTest {

    public static final String PATH_TO_KEYSTORE = "path/to/keystore";
    public static final String PASSWORD = "password";
    private static final KeyStoreConfig KEY_STORE_CONFIG = new KeyStoreConfig("name", PATH_TO_KEYSTORE,  PASSWORD, null, null);

    @Test(expected = SeedException.class)
    public void testKeyStoreLoaderMissingFile() {
        new KeyStoreLoader().load(KEY_STORE_CONFIG);
    }

    @Test
    public void testKeyStoreLoaderMissingName() {
        try {
            new KeyStoreLoader().load(new KeyStoreConfig(null, PATH_TO_KEYSTORE, PASSWORD, "type", "provider"));
        } catch (SeedException se) {
            if (!se.getErrorCode().equals(CryptoErrorCodes.KEYSTORE_CONFIGURATION_ERROR)) {
                fail();
            }
        }
    }

    @Test
    public void testKeyStoreLoaderMissingPath() {
        try {
            new KeyStoreLoader().load(new KeyStoreConfig("name", null, PASSWORD, "type", "provider"));
        } catch (SeedException se) {
            if (!se.getErrorCode().equals(CryptoErrorCodes.KEYSTORE_CONFIGURATION_ERROR)) {
                fail();
            }
        }
    }

    @Test
    public void testKeyStoreLoaderMissingPassword() {
        try {
            new KeyStoreLoader().load(new KeyStoreConfig("name", PATH_TO_KEYSTORE, "", "type", "provider"));
        } catch (SeedException se) {
            if (!se.getErrorCode().equals(CryptoErrorCodes.KEYSTORE_CONFIGURATION_ERROR)) {
                fail();
            }
        }
    }

    // TODO avoid to mock fileInputStream this causes issues with the logger
    @Test
    public void testKeyStoreLoader(@Mocked LoggerFactory loggerFactory, @Mocked final KeyStore mockedKeyStore,
                                   @Mocked final FileInputStream fileInputStream) throws Exception {
        new Expectations() {
            {
                new FileInputStream(PATH_TO_KEYSTORE);
                result = fileInputStream;
                mockedKeyStore.load(fileInputStream, PASSWORD.toCharArray());
            }
        };
        KeyStore keyStore = new KeyStoreLoader().load(KEY_STORE_CONFIG);
        Assertions.assertThat(keyStore).isNotNull();
    }

    @Test(expected = SeedException.class)
    public void testKeyStoreLoaderMissingProvider(@Mocked LoggerFactory loggerFactory, @Mocked final FileInputStream fileInputStream) {
        new KeyStoreLoader().load(new KeyStoreConfig("name", PATH_TO_KEYSTORE, PASSWORD, "jks", "provider"));
    }

    @Test
    public void testKeyStoreLoaderWithProvider(@Mocked LoggerFactory loggerFactory, @Mocked final KeyStore mockedKeyStore,
                                               @Mocked final FileInputStream fileInputStream) throws Exception {
        new Expectations() {
            {
                new FileInputStream(PATH_TO_KEYSTORE);
                result = fileInputStream;
                mockedKeyStore.load(fileInputStream, PASSWORD.toCharArray());
            }
        };
        KeyStore keyStore = new KeyStoreLoader().load(new KeyStoreConfig("name", PATH_TO_KEYSTORE, PASSWORD, "jks", "provider"));
        Assertions.assertThat(keyStore).isNotNull();
    }
}
