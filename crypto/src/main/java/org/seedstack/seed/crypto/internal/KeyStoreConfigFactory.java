/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import org.apache.commons.configuration.Configuration;

import java.util.Iterator;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class KeyStoreConfigFactory {

    private static final String KEYSTORE_PREFIX = "keystore";
    private static final String PATH_PREFIX = "path";
    private static final String PASSWORD_PREFIX = "password";
    private static final String TYPE_PREFIX = "type";
    private static final String PROVIDER_PREFIX = "provider";
    private static final String ALIAS_PREFIX = "alias";
    private final Configuration cryptoConfiguration;

    /**
     * Constructs a factory which allows to extract KeyStore configurations.
     *
     * @param cryptoConfiguration the crypto configuration (all the config under the crypto prefix)
     */
    public KeyStoreConfigFactory(Configuration cryptoConfiguration) {
        this.cryptoConfiguration = cryptoConfiguration;
    }

    /**
     * Indicates whether a KeyStore is configured.
     *
     * @param name the KeyStore name
     * @return true if the KeyStore is configured
     */
    public boolean isKeyStoreConfigured(String name) {
        return cryptoConfiguration.containsKey(KEYSTORE_PREFIX + "." + name + "." + PATH_PREFIX);
    }

    /**
     * Creates a KeyStoreConfig from the configuration.
     *
     * @param ksName              the KeyStore name
     * @return the KeyStore configuration
     */
    public KeyStoreConfig create(String ksName) {
        Configuration ksConfiguration = cryptoConfiguration.subset(KEYSTORE_PREFIX + "." + ksName);

        String path = ksConfiguration.getString(PATH_PREFIX);
        String password = ksConfiguration.getString(PASSWORD_PREFIX);
        String type = ksConfiguration.getString(TYPE_PREFIX);
        String provider = ksConfiguration.getString(PROVIDER_PREFIX);

        KeyStoreConfig keyStoreConfig = new KeyStoreConfig(ksName, path, password, type, provider);
        addAlias(keyStoreConfig, ksConfiguration);
        return keyStoreConfig;
    }

    private void addAlias(KeyStoreConfig keyStoreConfig, Configuration ksConfiguration) {
        Iterator<String> aliasIterator = ksConfiguration.getKeys(ALIAS_PREFIX);
        while (aliasIterator.hasNext()) {
            String aliasKey = aliasIterator.next();
            String alias = getAliasName(aliasKey);
            String password = getPassword(ksConfiguration, aliasKey);

            if (alias != null && password != null) {
                keyStoreConfig.addAliasPassword(alias, password);
            }
        }
    }

    private String getAliasName(String aliasKey) {
        String alias = null;
        String[] splittedKey = aliasKey.split("\\.");
        if (splittedKey.length == 3) {
            alias = splittedKey[1];
        }
        return alias;
    }

    private String getPassword(Configuration ksConfiguration, String aliasKey) {
        String password = null;
        if (aliasKey.endsWith("." + PASSWORD_PREFIX)) {
            password = ksConfiguration.getString(aliasKey);
        }
        return password;
    }

}
