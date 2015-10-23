/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import com.google.inject.Key;
import com.google.inject.name.Names;
import mockit.Expectations;
import mockit.Mocked;
import org.apache.commons.configuration.Configuration;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.crypto.api.EncryptionService;
import org.seedstack.seed.crypto.internal.fixtures.AliasQualifier;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class EncryptionServiceBindingFactoryTest {

    public static final String KEY_STORE_NAME = "keyStoreName";
    public static final String ALIAS_NAME = "aliasName";
    public static final String PASSWORD = "password";
    public static final String KEYSTORE_PATH = "path/to/keystore";

    @Mocked
    private KeyStore keyStore;
    @Mocked
    private Configuration configuration;
    @Mocked
    private EncryptionService encryptionService;
    @Mocked
    private EncryptionServiceFactory encryptionServiceFactory;
    @Mocked
    private Map<String, KeyStore> keyStores;

    @Test
    public void test_encryption_service_collect_never_return_null() {
        EncryptionServiceBindingFactory encryptionService = new EncryptionServiceBindingFactory();
        Map<Key<EncryptionService>, EncryptionService> encryptionServiceMap = encryptionService.createBindings(null, null, null);
        Assertions.assertThat(encryptionServiceMap).isNotNull();
    }

    @Test
    public void test_encryption_service_collect() {
        prepareMock(null);

        EncryptionServiceBindingFactory collector = new EncryptionServiceBindingFactory();
        Map<Key<EncryptionService>, EncryptionService> encryptionServiceMap = collector.createBindings(configuration, prepareKeyStoreConfigs(ALIAS_NAME, ALIAS_NAME + "2"), keyStores);
        Assertions.assertThat(encryptionServiceMap).hasSize(2);
        Assertions.assertThat(encryptionServiceMap).containsKey(Key.get(EncryptionService.class, Names.named(ALIAS_NAME)));
        Assertions.assertThat(encryptionServiceMap).containsKey(Key.get(EncryptionService.class, Names.named(ALIAS_NAME + "2")));
        Assertions.assertThat(encryptionServiceMap).containsValue(encryptionService);
    }

    @Test
    public void test_encryption_service_collect_with_qualifier() {
        prepareMock("foo");

        EncryptionServiceBindingFactory collector = new EncryptionServiceBindingFactory();
        Map<Key<EncryptionService>, EncryptionService> encryptionServiceMap = collector.createBindings(configuration, prepareKeyStoreConfigs(ALIAS_NAME), keyStores);
        Assertions.assertThat(encryptionServiceMap).hasSize(1);
        Assertions.assertThat(encryptionServiceMap).containsKey(Key.get(EncryptionService.class, Names.named("foo")));
        Assertions.assertThat(encryptionServiceMap).containsValue(encryptionService);
    }

    @Test
    public void test_encryption_service_collect_with_qualifier_annotation() throws ClassNotFoundException {
        prepareMock("org.seedstack.seed.crypto.internal.fixtures.AliasQualifier");

        EncryptionServiceBindingFactory collector = new EncryptionServiceBindingFactory();
        Map<Key<EncryptionService>, EncryptionService> encryptionServiceMap = collector.createBindings(configuration, prepareKeyStoreConfigs(ALIAS_NAME), keyStores);
        Assertions.assertThat(encryptionServiceMap).containsKey(Key.get(EncryptionService.class, AliasQualifier.class));
    }

    @Test(expected = SeedException.class)
    public void test_encryption_service_collect_with_wrong_qualifier_class() throws ClassNotFoundException {
        prepareMock("org.seedstack.seed.crypto.internal.fixtures.BadAliasQualifier"); // interface instead of annotation
        new EncryptionServiceBindingFactory().createBindings(configuration, prepareKeyStoreConfigs(ALIAS_NAME), keyStores);
    }

    @Test(expected = SeedException.class)
    public void test_encryption_service_collect_with_wrong_qualifier_annotation() throws ClassNotFoundException {
        prepareMock("org.seedstack.seed.crypto.internal.fixtures.BadAliasQualifier2"); // missing @Qualifier
        new EncryptionServiceBindingFactory().createBindings(configuration, prepareKeyStoreConfigs(ALIAS_NAME), keyStores);
    }

    private void prepareMock(final String qualifier) {
        new Expectations() {
            {
                new EncryptionServiceFactory(configuration, keyStore);
                result = encryptionServiceFactory;

                encryptionServiceFactory.create(ALIAS_NAME, PASSWORD.toCharArray());
                result = encryptionService;

                configuration.getString("keystore.keyStoreName.alias.aliasName.qualifier");
                result = qualifier;
            }
        };
    }

    private Map<String, KeyStoreConfig> prepareKeyStoreConfigs(String... aliasNames) {
        Map<String, KeyStoreConfig> keyStoreConfigs = new HashMap<String, KeyStoreConfig>();
        KeyStoreConfig keyStoreConfig = new KeyStoreConfig(KEY_STORE_NAME, KEYSTORE_PATH, PASSWORD, null, null);
        for (String aliasName : aliasNames) {
            keyStoreConfig.addAliasPassword(aliasName, PASSWORD);
        }
        keyStoreConfigs.put(KEY_STORE_NAME, keyStoreConfig);
        return keyStoreConfigs;
    }
}
