/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.crypto;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.Optional;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.EncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to create a new {@link EncryptionService} object. This factory checks the KeyStore state (connection)
 * if a KeyStore is used.
 */
class EncryptionServiceFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionServiceFactory.class);
    private static final String DEFAULT_CERTIFICATE_TYPE = "X.509";
    private final KeyStore keyStore;

    /**
     * Constructs an encryption service factory for a specific KeyStore.
     *
     * @param keyStore the KeyStore which holds the key pairs
     */
    EncryptionServiceFactory(KeyStore keyStore) {
        if (keyStore == null) {
            throw SeedException.createNew(CryptoErrorCode.NO_KEYSTORE_CONFIGURED);
        }
        this.keyStore = keyStore;
    }

    /**
     * Creates an encryption service for the key pair associated to the alias.
     * <p>
     * The certificate will be loaded from an external file if a location is specified
     * in the configuration.
     * </p>
     *
     * @param alias    the alias corresponding to the key store entry
     * @param password the password protecting the private key
     * @return encryption service
     */
    EncryptionService create(String alias, char[] password) {
        PublicKey pk = getPublicKey(alias);

        Key key;
        try {
            key = this.keyStore.getKey(alias, password);
        } catch (UnrecoverableKeyException e) {
            throw SeedException.wrap(e, CryptoErrorCode.UNRECOVERABLE_KEY);
        } catch (KeyStoreException e) {
            throw SeedException.wrap(e, CryptoErrorCode.UNEXPECTED_EXCEPTION);
        } catch (NoSuchAlgorithmException e) {
            throw SeedException.wrap(e, CryptoErrorCode.ALGORITHM_CANNOT_BE_FOUND);
        }

        return new EncryptionServiceImpl(alias, pk, key);
    }

    /**
     * Creates an encryption service for the certificate associated to the alias.
     * No private key will be associated as no password is provided.
     * <p>
     * The certificate will be loaded from an external file if a location is specified
     * in the configuration.
     * </p>
     *
     * @param alias the alias corresponding to the key store entry
     * @return encryption service
     */
    EncryptionService create(String alias) {
        return new EncryptionServiceImpl(alias, getPublicKey(alias), null);
    }

    private PublicKey getPublicKey(String alias) {
        try {
            return Optional.ofNullable(keyStore.getCertificate(alias))
                    .map(Certificate::getPublicKey)
                    .orElse(null);
        } catch (KeyStoreException e) {
            throw SeedException.createNew(CryptoErrorCode.NO_KEYSTORE_CONFIGURED);
        }
    }
}
