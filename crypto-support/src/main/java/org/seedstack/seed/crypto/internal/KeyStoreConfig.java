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

import java.util.HashMap;
import java.util.Map;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class KeyStoreConfig {

    private final String name;
    private final String path;
    private final String password;
    private final String type;
    private final String provider;

    private final Map<String, String> aliasPasswords = new HashMap<String, String>();

    public KeyStoreConfig(String name, String path, String password, String type, String provider) {
        this.name = name;
        this.path = path;
        this.password = password;
        this.type = type;
        this.provider = provider;
    }

    public void addAliasPassword(String alias, String password) {
        aliasPasswords.put(alias, password);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getPassword() {
        return password;
    }

    public String getType() {
        return type;
    }

    public String getProvider() {
        return provider;
    }

    public Map<String, String> getAliasPasswords() {
        return aliasPasswords;
    }
}
