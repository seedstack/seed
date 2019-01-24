/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.crypto;

import com.google.common.collect.Lists;
import com.google.inject.Key;
import com.google.inject.name.Names;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mockit.Expectations;
import mockit.Mocked;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.crypto.fixtures.AliasQualifier;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.seed.crypto.EncryptionService;

public class EncryptionServiceBindingFactoryTest {

    private static final String KEY_STORE_NAME = "keyStoreName";
    private static final String ALIAS_NAME = "aliasName";
    private static final String PASSWORD = "password";
    public static final String KEYSTORE_PATH = "path/to/keystore";

    @Mocked
    private KeyStore keyStore;
    @Mocked
    private CryptoConfig configuration;
    @Mocked
    private EncryptionService encryptionService;
    @Mocked
    private EncryptionServiceFactory encryptionServiceFactory;
    @Mocked
    private Map<String, KeyStore> keyStores;

    @Test
    public void test_encryption_service_collect_never_return_null() {
        EncryptionServiceBindingFactory encryptionService = new EncryptionServiceBindingFactory();
        Map<Key<EncryptionService>, EncryptionService> encryptionServiceMap = encryptionService.createBindings(null,
                null);
        Assertions.assertThat(encryptionServiceMap).isNotNull();
    }

    @Test
    public void test_encryption_service_collect() {
        prepareMock();

        EncryptionServiceBindingFactory collector = new EncryptionServiceBindingFactory();
        Map<Key<EncryptionService>, EncryptionService> encryptionServiceMap = collector.createBindings(prepareKeyPairs(
                ALIAS_NAME,
                ALIAS_NAME + "2"), keyStores);
        Assertions.assertThat(encryptionServiceMap).hasSize(2);
        Assertions.assertThat(encryptionServiceMap).containsKey(
                Key.get(EncryptionService.class, Names.named(ALIAS_NAME)));
        Assertions.assertThat(encryptionServiceMap).containsKey(
                Key.get(EncryptionService.class, Names.named(ALIAS_NAME + "2")));
        Assertions.assertThat(encryptionServiceMap).containsValue(encryptionService);
    }

    @Test
    public void test_encryption_service_collect_with_qualifier() {
        prepareMock();

        EncryptionServiceBindingFactory collector = new EncryptionServiceBindingFactory();
        Map<Key<EncryptionService>, EncryptionService> encryptionServiceMap = collector.createBindings(
                prepareKeyPairWithQualifier("foo"),
                keyStores);
        Assertions.assertThat(encryptionServiceMap).hasSize(1);
        Assertions.assertThat(encryptionServiceMap).containsKey(Key.get(EncryptionService.class, Names.named("foo")));
        Assertions.assertThat(encryptionServiceMap).containsValue(encryptionService);
    }

    @Test
    public void test_encryption_service_collect_alias_without_private_key() {
        new Expectations() {
            {
                new EncryptionServiceFactory(keyStore);
                result = encryptionServiceFactory;

                encryptionServiceFactory.create(ALIAS_NAME);
                result = encryptionService;
            }
        };

        EncryptionServiceBindingFactory collector = new EncryptionServiceBindingFactory();
        List<KeyPairConfig> keyPairConfigurations = Lists.newArrayList(
                new KeyPairConfig(KEY_STORE_NAME, ALIAS_NAME, null, "foo"));
        Map<Key<EncryptionService>, EncryptionService> encryptionServiceMap = collector.createBindings(
                keyPairConfigurations,
                keyStores);
        Assertions.assertThat(encryptionServiceMap).hasSize(1);
        Assertions.assertThat(encryptionServiceMap).containsKey(Key.get(EncryptionService.class, Names.named("foo")));
        Assertions.assertThat(encryptionServiceMap).containsValue(encryptionService);
    }

    @Test
    public void test_encryption_service_collect_with_qualifier_annotation() {
        prepareMock();
        List<KeyPairConfig> keyPairConfigurations = prepareKeyPairWithQualifier(
                "org.seedstack.seed.core.internal.crypto.fixtures.AliasQualifier");

        EncryptionServiceBindingFactory collector = new EncryptionServiceBindingFactory();
        Map<Key<EncryptionService>, EncryptionService> encryptionServiceMap = collector.createBindings(
                keyPairConfigurations,
                keyStores);
        Assertions.assertThat(encryptionServiceMap).containsKey(Key.get(EncryptionService.class, AliasQualifier.class));
    }

    @Test(expected = SeedException.class)
    public void test_encryption_service_collect_with_wrong_qualifier_class() {
        prepareMock();
        // interface instead of annotation
        List<KeyPairConfig> keyPairConfigurations = prepareKeyPairWithQualifier(
                "org.seedstack.seed.core.internal.crypto.fixtures.BadAliasQualifier");
        new EncryptionServiceBindingFactory().createBindings(keyPairConfigurations, keyStores);
    }

    @Test(expected = SeedException.class)
    public void test_encryption_service_collect_with_wrong_qualifier_annotation() {
        prepareMock();
        // missing @Qualifier
        List<KeyPairConfig> keyPairConfigurations = prepareKeyPairWithQualifier(
                "org.seedstack.seed.core.internal.crypto.fixtures.BadAliasQualifier2");
        new EncryptionServiceBindingFactory().createBindings(keyPairConfigurations, keyStores);
    }

    private void prepareMock() {
        new Expectations() {
            {
                new EncryptionServiceFactory(keyStore);
                result = encryptionServiceFactory;

                encryptionServiceFactory.create(ALIAS_NAME, PASSWORD.toCharArray());
                result = encryptionService;
            }
        };
    }

    private List<KeyPairConfig> prepareKeyPairs(String... aliasNames) {
        List<KeyPairConfig> keyPairConfigs = new ArrayList<>();
        for (String aliasName : aliasNames) {
            keyPairConfigs.add(new KeyPairConfig(KEY_STORE_NAME, aliasName, PASSWORD, null));
        }
        return keyPairConfigs;
    }

    private List<KeyPairConfig> prepareKeyPairWithQualifier(String qualifier) {
        return Lists.newArrayList(new KeyPairConfig(KEY_STORE_NAME, ALIAS_NAME, PASSWORD, qualifier));
    }
}
