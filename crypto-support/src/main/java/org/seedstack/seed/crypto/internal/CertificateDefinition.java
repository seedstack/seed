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
 * Creation : 11 mai 2015
 */
package org.seedstack.seed.crypto.internal;

import javax.security.cert.X509Certificate;

/**
 * Definition which contains all parameters to encrypt or decrypt a text. So informations are for the private key and for the certificate.
 * 
 * @author thierry.bouvet@mpsa.com
 */
class CertificateDefinition {

    private String alias;
    private X509Certificate certificate;
    private String password;

    /**
     * Key alias in the keystore for the private key (used to decrypt a text).
     * 
     * @return the key alias in the keystore
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Set the Key alias in the keystore for the private key (used to decrypt a text).
     * 
     * @param alias the key alias in the keystore
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * {@link X509Certificate} to use to encrypt text.
     * 
     * @return the {@link X509Certificate} to use to encrypt text
     */
    public X509Certificate getCertificate() {
        return certificate;
    }

    /**
     * Set the {@link X509Certificate} to use to encrypt text.
     * 
     * @param certificate the {@link X509Certificate} to use to encrypt text
     */
    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

    /**
     * Password for the private key (used to decrypt a text).
     * 
     * @return the password to use to read the private key
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password for the private key (used to decrypt a text).
     * 
     * @param password the password to use to read the private key
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
