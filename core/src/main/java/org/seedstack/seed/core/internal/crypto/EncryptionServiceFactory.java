/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.crypto;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.seed.crypto.EncryptionService;
import org.seedstack.shed.ClassLoaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to create a new {@link EncryptionService} object. This factory checks the KeyStore state (connection)
 * if a KeyStore is used.
 */
class EncryptionServiceFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionServiceFactory.class);
    private static final String DEFAULT_CERTIFICATE_TYPE = "X.509";
    private final CryptoConfig cryptoConfig;
    private final KeyStore keyStore;

    /**
     * Constructs an encryption service factory for a specific KeyStore.
     *
     * @param cryptoConfig the crypto configuration
     * @param keyStore     the KeyStore which holds the key pairs
     */
    EncryptionServiceFactory(CryptoConfig cryptoConfig, KeyStore keyStore) {
        this.cryptoConfig = cryptoConfig;
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
        Certificate certificate;

        // Look for an external certificate
        String certLocation = getCertificateLocation(alias);
        if (certLocation != null) {
            certificate = loadCertificateFromFile(certLocation);
        } else {
            // Otherwise load the certificate from the key store
            try {
                certificate = keyStore.getCertificate(alias);
            } catch (KeyStoreException e) {
                throw SeedException.createNew(CryptoErrorCode.NO_KEYSTORE_CONFIGURED);
            }
        }

        if (certificate != null) {
            return certificate.getPublicKey();
        }

        return null;
    }

    private String getCertificateLocation(String alias) {
        CryptoConfig.CertificateConfig certificateConfig = cryptoConfig.certificates().get(alias);

        if (certificateConfig != null) {
            // Find the certificate location from the classpath
            String resource = certificateConfig.getResource();
            if (resource != null) {
                URL urlResource = ClassLoaders.findMostCompleteClassLoader(EncryptionServiceFactory.class)
                        .getResource(resource);
                if (urlResource == null) {
                    throw SeedException.createNew(CryptoErrorCode.CERTIFICATE_NOT_FOUND)
                            .put("alias", alias).put("certResource", resource);
                }
                return urlResource.getFile();
            } else {
                // Otherwise get the file path from the configuration
                return certificateConfig.getFile();
            }
        } else {
            return null;
        }
    }

    private Certificate loadCertificateFromFile(String certLocation) {
        Certificate certificate = null;
        if (certLocation != null) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(certLocation);
                certificate = CertificateFactory.getInstance(DEFAULT_CERTIFICATE_TYPE).generateCertificate(in);
            } catch (Exception e) {
                throw SeedException.wrap(e, CryptoErrorCode.UNABLE_TO_READ_CERTIFICATE)
                        .put("location", certLocation);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    LOGGER.warn("Unable to close certificate input stream", e);
                }
            }
        }
        return certificate;
    }
}
