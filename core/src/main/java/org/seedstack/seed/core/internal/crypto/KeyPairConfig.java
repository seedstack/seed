/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.crypto;

class KeyPairConfig {
    private final String alias;
    private final String password;
    private final String qualifier;
    private final String keyStoreName;

    KeyPairConfig(String keyStoreName, String alias, String password, String qualifier) {
        this.keyStoreName = keyStoreName;
        this.alias = alias;
        this.password = password;
        this.qualifier = qualifier;
    }

    public String getAlias() {
        return alias;
    }

    String getPassword() {
        return password;
    }

    String getQualifier() {
        return qualifier;
    }

    String getKeyStoreName() {
        return keyStoreName;
    }
}
