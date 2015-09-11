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

import org.seedstack.seed.crypto.api.EncryptionService;

import java.security.KeyStore;

/**
 * Factory to create a new {@link EncryptionService} object. This factory checks the keystore state (connection) if a keystore is used.
 *
 * @author thierry.bouvet@mpsa.com
 */
class EncryptionServiceFactory {

    /**
     * Creates an encryption service for the given key.
     *
     * @param keyDefinition the key definition. Contains all the key information
     */
    EncryptionService createEncryptionService(KeyDefinition keyDefinition) {
        return new EncryptionServiceImpl(new JCAInitializer().loadKeyStore(keyDefinition.getKeyStoreDefinition()), keyDefinition);
    }

    /**
     * Creates an encryption service for the master key.
     */
    EncryptionService createEncryptionService() {
        JCADefinitionFactory jcaDefinitionFactory = new JCADefinitionFactory();
        KeyStoreDefinition masterKSDefinition = jcaDefinitionFactory.getMasterKeyStoreDefinition();
        if (masterKSDefinition == null) {
            // TODO <pith> 09/09/2015: add SE
            throw new IllegalStateException("No master KeyStore has been configured");
        }
        KeyStore ks = new JCAInitializer().loadKeyStore(masterKSDefinition);
        return new EncryptionServiceImpl(ks, jcaDefinitionFactory.getMasterKeyDefinition());
    }
}
