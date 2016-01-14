/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class KeyPairConfig {

    private final String alias;
    private final String password;
    private final String certificateLocation;
    private final String qualifier;
    private final String keyStoreName;

    public KeyPairConfig(String keyStoreName, String alias, String password, String certificateLocation, String qualifier) {
        this.keyStoreName = keyStoreName;
        this.alias = alias;
        this.password = password;
        this.certificateLocation = certificateLocation;
        this.qualifier = qualifier;
    }

    public String getAlias() {
        return alias;
    }

    public String getPassword() {
        return password;
    }

    public String getCertificateLocation() {
        return certificateLocation;
    }

    public String getQualifier() {
        return qualifier;
    }

    public String getKeyStoreName() {
        return keyStoreName;
    }
}
