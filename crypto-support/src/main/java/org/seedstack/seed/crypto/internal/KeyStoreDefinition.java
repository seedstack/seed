/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 12 mai 2015
 */
package org.seedstack.seed.crypto.internal;

import javax.annotation.Nullable;

/**
 * Definition which contains all parameters to access to the keystore.
 *
 * @author thierry.bouvet@mpsa.com
 */
class KeyStoreDefinition {

    private final String path;
    private final String password;
    private final String type;
    private final String provider;

    /**
     * Constructor.
     *
     * @param path the keyStore path
     * @param password the keyStore password
     */
    public KeyStoreDefinition(String path, String password) {
        this(path, password, null, null);
    }

    /**
     * Constructor.
     *
     * @param path the keyStore path
     * @param password the keyStore password
     * @param type the keyStore type
     */
    public KeyStoreDefinition(String path, String password, @Nullable String type) {
        this(path, password, type, null);
    }

    /**
     * Constructor.
     *
     * @param path the keyStore path
     * @param password the keyStore password
     * @param type the keyStore type
     * @param provider the provider name
     */
    public KeyStoreDefinition(String path, String password, @Nullable String type, @Nullable String provider) {
        this.path = path;
        this.password = password;
        if (type == null || type.equals("")) {
            this.type = "JKS";
        } else {
            this.type = type;
        }
        this.provider = provider;
    }

    /**
     * The keystore path.
     *
     * @return the keystore path
     */
    public String getPath() {
        return path;
    }

    /**
     * Password to access to the keystore.
     *
     * @return the password to access to the keystore
     */
    public String getPassword() {
        return password;
    }

    /**
     * The type of keystore. The type is JKS by default.
     *
     * @return the keystore type
     * @see <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyStore">Standard names</a>
     */
    public String getType() {
        return type;
    }

    /**
     * The provider name. The provider is null by default.
     *
     * @return the provider name
     */
    public String getProvider() {
        return provider;
    }
}
