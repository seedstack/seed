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
 * Creation : 4 juin 2015
 */
/**
 * 
 */
package org.seedstack.seed.crypto.internal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.seedstack.seed.crypto.api.EncryptionService;

/**
 * Factory to create a new {@link EncryptionService} object. This factory checks the keystore state (connection) if a keystore is used.
 * 
 * @author thierry.bouvet@mpsa.com
 */
class EncryptionServiceFactory {

    public EncryptionService getInstance(KeyStoreDefinition keyStoreDefinition, CertificateDefinition certificateDefinition) {
        KeyStore keyStore = loadKeystore(keyStoreDefinition);
        return new EncryptionServiceImpl(keyStore, certificateDefinition);
    }

    private KeyStore loadKeystore(KeyStoreDefinition keyStoreDefinition) {
        if (keyStoreDefinition == null || keyStoreDefinition.getPath() == null) {
            return null;
        }
        KeyStore ks;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            throw new RuntimeException("Provider problem for the keystore", e);
        }
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(keyStoreDefinition.getPath());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("The keystore cannot be found !", e);
        }
        try {
            ks.load(inputStream, keyStoreDefinition.getPassword().toCharArray());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("The algorithm used to check the integrity of the keystore cannot be found !", e);
        } catch (CertificateException e) {
            throw new RuntimeException("The certificates in the keystore could not be loaded", e);
        } catch (IOException e) {
            throw new RuntimeException("The given password is incorrect", e);
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Can not close the keystore", e);
        }
        return ks;

    }

}
