/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import org.junit.Test;
import org.seedstack.seed.SeedException;

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

    @Test(expected = SeedException.class)
    public void testKeyStoreLoaderMissingProvider() {
        new KeyStoreLoader().load(new KeyStoreConfig("name", PATH_TO_KEYSTORE, PASSWORD, "jks", "provider"));
    }
}
