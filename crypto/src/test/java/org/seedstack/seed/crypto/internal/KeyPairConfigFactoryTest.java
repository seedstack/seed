/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import jodd.util.collection.ArrayEnumeration;
import mockit.Expectations;
import mockit.MockUp;
import mockit.Mocked;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.util.Enumeration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.seedstack.seed.core.utils.ConfigurationUtils.buildKey;
import static org.seedstack.seed.crypto.internal.CryptoPlugin.ALIAS;
import static org.seedstack.seed.crypto.internal.CryptoPlugin.KEYSTORE;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class KeyPairConfigFactoryTest {

    public static final String KEY_STORE_NAME = "keystoreName";
    public static final String ALIAS_NAME_1 = "aliasName1";
    public static final String ALIAS_NAME_2 = "aliasName2";
    public static final String PASSWORD = "aliasPassword";
    public static final String CERT_RESOURCE = "certResource";
    public static final String PATH_TO_CERT = "path/to/cert";
    public static final String ALIAS_QUALIFIER = "aliasQualifier";

    @Mocked
    private KeyStore keyStore;

    @Mocked
    private Configuration configuration;

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
                configuration.getString(buildKey(KEYSTORE, KEY_STORE_NAME, ALIAS, ALIAS_NAME_1, CryptoPlugin.PASSWORD));
                result = PASSWORD;
                configuration.getString(buildKey(KEYSTORE, KEY_STORE_NAME, ALIAS, ALIAS_NAME_2, CryptoPlugin.PASSWORD));
                result = PASSWORD;
                configuration.getString(buildKey(KEYSTORE, KEY_STORE_NAME, ALIAS, ALIAS_NAME_2, CryptoPlugin.QUALIFIER));
                result = ALIAS_QUALIFIER;
            }
        };

        List<KeyPairConfig> keyPairConfigs = underTest.create(KEY_STORE_NAME, keyStore);

        assertThat(keyPairConfigs.get(0).getPassword()).isEqualTo(PASSWORD);

        assertThat(keyPairConfigs.get(1).getPassword()).isEqualTo(PASSWORD);
        assertThat(keyPairConfigs.get(1).getQualifier()).isEqualTo(ALIAS_QUALIFIER);
    }

    @Test
    public void test_key_pair_config_create_with_external_certificate(final @Mocked URL url) throws Exception {
        new MockUp<ClassLoader>() {
            @mockit.Mock
            public URL getResource(String name) {
                try {
                    return new URL(CERT_RESOURCE);
                } catch (MalformedURLException e) {
                    throw new IllegalStateException(e);
                }
            }
        };

        prepareTwoAliases();

        new Expectations() {
            {
                configuration.getString(buildKey(CryptoPlugin.CERT, ALIAS_NAME_1, CryptoPlugin.CERT_FILE));
                result = PATH_TO_CERT;
                configuration.getString(buildKey(CryptoPlugin.CERT, ALIAS_NAME_2, CryptoPlugin.CERT_RESOURCE));
                result = CERT_RESOURCE;
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
                Enumeration<String> aliases = new ArrayEnumeration<String>(new String[]{ALIAS_NAME_1, ALIAS_NAME_2});
                keyStore.aliases();
                result = aliases;
            }
        };
    }
}
