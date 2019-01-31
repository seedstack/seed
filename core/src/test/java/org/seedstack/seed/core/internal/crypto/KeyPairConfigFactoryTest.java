/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.security.KeyStore;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.shed.ClassLoaders;

public class KeyPairConfigFactoryTest {
    private static final String KEY_STORE_NAME = "keystoreName";
    private static final String ALIAS_NAME_1 = "aliasName1";
    private static final String ALIAS_NAME_2 = "aliasName2";
    private static final String PASSWORD = "aliasPassword";
    private static final String CERT_RESOURCE = "certResource";
    private static final String PATH_TO_CERT = "path/to/cert";
    private static final String ALIAS_QUALIFIER = "aliasQualifier";

    @Mocked
    private KeyStore keyStore;
    @Mocked
    private CryptoConfig configuration;

    private KeyPairConfigFactory underTest;

    @Before
    public void before() {
        underTest = new KeyPairConfigFactory(configuration);
    }

    @Test
    public void test_key_pair_config_create_never_return_null() {
        List<KeyPairConfig> keyPairConfigs = underTest.create(KEY_STORE_NAME, keyStore);
        assertThat(keyPairConfigs).isNotNull();
        assertThat(keyPairConfigs).hasSize(0);
    }

    @Test
    public void test_key_pair_config_create_two_aliases_without_password() throws Exception {
        prepareTwoAliases();

        List<KeyPairConfig> keyPairConfigs = underTest.create(KEY_STORE_NAME, keyStore);

        assertThat(keyPairConfigs).hasSize(2);

        assertThat(keyPairConfigs.get(0).getKeyStoreName()).isEqualTo(KEY_STORE_NAME);
        assertThat(keyPairConfigs.get(0).getAlias()).isEqualTo(ALIAS_NAME_1);
        assertThat(keyPairConfigs.get(0).getPassword()).isNull();
        assertThat(keyPairConfigs.get(0).getCertificateLocation()).isNull();

        assertThat(keyPairConfigs.get(1).getKeyStoreName()).isEqualTo(KEY_STORE_NAME);
        assertThat(keyPairConfigs.get(1).getAlias()).isEqualTo(ALIAS_NAME_2);
        assertThat(keyPairConfigs.get(1).getCertificateLocation()).isNull();
    }

    @Test
    public void test_key_pair_config_create_two_aliases_with_password_and_qualifier() throws Exception {
        prepareTwoAliases();

        new Expectations() {
            {
                configuration.keyStores();
                result = new HashMap<String, CryptoConfig.KeyStoreConfig>() {{
                    put(KEY_STORE_NAME, new CryptoConfig.KeyStoreConfig()
                            .addAlias(ALIAS_NAME_1, new CryptoConfig.KeyStoreConfig.AliasConfig().setPassword(PASSWORD))
                            .addAlias(ALIAS_NAME_2,
                                    new CryptoConfig.KeyStoreConfig.AliasConfig().setPassword(PASSWORD).setQualifier(
                                            ALIAS_QUALIFIER)
                            ));
                }};
            }
        };

        List<KeyPairConfig> keyPairConfigs = underTest.create(KEY_STORE_NAME, keyStore);

        assertThat(keyPairConfigs.get(0).getPassword()).isEqualTo(PASSWORD);

        assertThat(keyPairConfigs.get(1).getPassword()).isEqualTo(PASSWORD);
        assertThat(keyPairConfigs.get(1).getQualifier()).isEqualTo(ALIAS_QUALIFIER);
    }

    @Test
    public void test_key_pair_config_create_with_external_certificate() throws Exception {
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

        prepareTwoAliases();

        new Expectations() {
            {
                configuration.certificates();
                result = new HashMap<String, CryptoConfig.CertificateConfig>() {{
                    put(ALIAS_NAME_1, new CryptoConfig.CertificateConfig().setFile(PATH_TO_CERT));
                    put(ALIAS_NAME_2, new CryptoConfig.CertificateConfig().setResource(CERT_RESOURCE));
                }};

                url.getFile();
                result = CERT_RESOURCE;
            }
        };

        List<KeyPairConfig> keyPairConfigs = underTest.create(KEY_STORE_NAME, keyStore);

        assertThat(keyPairConfigs.get(0).getCertificateLocation()).isEqualTo(PATH_TO_CERT);
        assertThat(keyPairConfigs.get(1).getCertificateLocation()).isEqualTo(CERT_RESOURCE);
    }

    private void prepareTwoAliases() throws Exception {
        new Expectations() {
            {
                Enumeration<String> aliases = new Vector<String>() {{
                    add(ALIAS_NAME_1);
                    add(ALIAS_NAME_2);
                }}.elements();
                keyStore.aliases();
                result = aliases;
            }
        };
    }
}
