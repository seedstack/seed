/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import org.seedstack.seed.core.api.SeedException;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains a key store configuration.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
class KeyStoreConfig {

    private final String name;
    private final String path;
    private final String password;
    private final String type;
    private final String provider;

    private final Map<String, String> aliasPasswords = new HashMap<String, String>();

    KeyStoreConfig(String name, String path, String password) {
        this(name, path, password, null, null);
    }

    KeyStoreConfig(String name, String path, String password, @Nullable  String type, @Nullable String provider) {
        if (name == null || "".equals(name) || path == null || "".equals(path) || password == null || "".equals(password)) {
            throw SeedException.createNew(CryptoErrorCodes.KEYSTORE_CONFIGURATION_ERROR)
                    .put("keyName", name)
                    .put("path", path)
                    .put("password", password);
        }

        this.name = name;
        this.path = path;
        this.password = password;
        this.type = type;
        this.provider = provider;
    }

    void addAliasPassword(String alias, String password) {
        aliasPasswords.put(alias, password);
    }

    String getName() {
        return name;
    }

    String getPath() {
        return path;
    }

    String getPassword() {
        return password;
    }

    String getType() {
        return type;
    }

    String getProvider() {
        return provider;
    }

    Map<String, String> getAliasPasswords() {
        return aliasPasswords;
    }
}
