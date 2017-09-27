/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.crypto;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.CryptoConfig;

public class KeyStoreLoaderTest {
    private static final String PATH_TO_KEYSTORE = "path/to/keystore";
    private static final String PASSWORD = "password";

    @Test
    public void testKeyStoreLoaderMissingFile() {
        try {
            new KeyStoreLoader().load("name",
                    new CryptoConfig.KeyStoreConfig().setPath(PATH_TO_KEYSTORE).setPassword(PASSWORD));
        } catch (SeedException se) {
            if (!se.getErrorCode().equals(CryptoErrorCode.KEYSTORE_NOT_FOUND)) {
                fail();
            }
        }
    }

    @Test
    public void testKeyStoreLoaderMissingName() {
        try {
            new KeyStoreLoader().load(null,
                    new CryptoConfig.KeyStoreConfig().setPath(PATH_TO_KEYSTORE).setPassword(PASSWORD).setType(
                            "type").setProvider("provider"));
        } catch (SeedException se) {
            if (!se.getErrorCode().equals(CryptoErrorCode.KEYSTORE_CONFIGURATION_ERROR)) {
                fail();
            }
        }
    }

    @Test
    public void testKeyStoreLoaderMissingPath() {
        try {
            new KeyStoreLoader().load("name",
                    new CryptoConfig.KeyStoreConfig().setPassword(PASSWORD).setType("type").setProvider("provider"));
        } catch (SeedException se) {
            if (!se.getErrorCode().equals(CryptoErrorCode.KEYSTORE_CONFIGURATION_ERROR)) {
                fail();
            }
        }
    }

    @Test
    public void testKeyStoreLoaderMissingPassword() {
        try {
            new KeyStoreLoader().load("name",
                    new CryptoConfig.KeyStoreConfig().setPath(PATH_TO_KEYSTORE).setType("type").setProvider(
                            "provider"));
        } catch (SeedException se) {
            if (!se.getErrorCode().equals(CryptoErrorCode.KEYSTORE_CONFIGURATION_ERROR)) {
                fail();
            }
        }
    }

    @Test(expected = SeedException.class)
    public void testKeyStoreLoaderMissingProvider() {
        new KeyStoreLoader().load("name",
                new CryptoConfig.KeyStoreConfig().setPath(PATH_TO_KEYSTORE).setPassword(PASSWORD).setType(
                        "jks").setProvider("provider"));
    }
}
