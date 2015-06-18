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

/**
 * Definition which contains all parameters to access to the keystore.
 * 
 * @author thierry.bouvet@mpsa.com
 */
public class KeyStoreDefinition {

    private String path;

    private String password;

    /**
     * The keystore path.
     * 
     * @return the keystore path
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the keystore path.
     * 
     * @param path the keystore path
     */
    public void setPath(String path) {
        this.path = path;
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
     * Set the password to access to the keystore.
     * 
     * @param password the password to access to the keystore
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
