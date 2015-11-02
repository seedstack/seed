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
import org.apache.commons.configuration.Configuration;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.core.api.SeedException;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class KeyStoreConfigFactoryTest {

    public static final String KEY_STORE_NAME = "keyStoreName";
    public static final String TYPE = "jks";
    public static final String PROVIDER = "provider";
    public static final String PATH_TO_KEYSTORE = "path/to/keystore";
    public static final String PASSWORD = "password";

    @Mocked
    private Configuration configuration;

    @Test
    public void test_key_store_config_factory() {
        new Expectations() {
            {
                configuration.getString("path");
                result = PATH_TO_KEYSTORE;
                configuration.getString("password");
                result = PASSWORD;
                configuration.getString("provider");
                result = PROVIDER;
                configuration.getString("type");
                result = TYPE;
            }
        };

        KeyStoreConfigFactory factory = new KeyStoreConfigFactory(configuration);
        KeyStoreConfig keyStoreConfig = factory.create(KEY_STORE_NAME);

        Assertions.assertThat(keyStoreConfig.getName()).isEqualTo(KEY_STORE_NAME);
        Assertions.assertThat(keyStoreConfig.getPassword()).isEqualTo(PASSWORD);
        Assertions.assertThat(keyStoreConfig.getPath()).isEqualTo(PATH_TO_KEYSTORE);
        Assertions.assertThat(keyStoreConfig.getProvider()).isEqualTo(PROVIDER);
        Assertions.assertThat(keyStoreConfig.getType()).isEqualTo(TYPE);
    }

    @Test(expected = SeedException.class)
    public void test_key_store_config_factory_without_path() {
        new KeyStoreConfigFactory(configuration).create(KEY_STORE_NAME);
    }

    @Test(expected = SeedException.class)
    public void test_key_store_config_factory_without_password() {
        new Expectations() {
            {
                configuration.getString("path");
                result = PATH_TO_KEYSTORE;
            }
        };
        new KeyStoreConfigFactory(configuration).create(KEY_STORE_NAME);
    }
}
