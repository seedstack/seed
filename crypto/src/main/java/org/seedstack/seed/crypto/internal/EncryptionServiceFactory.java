/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
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

import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.utils.ConfigurationUtils;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.crypto.api.EncryptionService;

import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

/**
 * Factory to create a new {@link EncryptionService} object. This factory checks the KeyStore state (connection)
 * if a KeyStore is used.
 *
 * @author thierry.bouvet@mpsa.com
 */
class EncryptionServiceFactory {

    public static final String CERT = "cert";
    public static final String CERT_FILE = "file";
    public static final String CERT_RESOURCE = "resource";
    public static final String DEFAULT_CERTIFICATE_TYPE = "X.509";

    private final Configuration configuration;
    private final KeyStore keyStore;

    /**
     * Constructs an encryption service factory for a specific KeyStore.
     *
     * @param configuration the crypto configuration
     * @param keyStore      the KeyStore which holds the key pairs
     */
    EncryptionServiceFactory(Configuration configuration, KeyStore keyStore) {
        this.configuration = configuration;
        if (keyStore == null) {
            throw SeedException.createNew(CryptoErrorCodes.NO_KEYSTORE_CONFIGURED);
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
            throw SeedException.wrap(e, CryptoErrorCodes.UNRECOVERABLE_KEY);
        } catch (KeyStoreException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.UNEXPECTED_EXCEPTION);
        } catch (NoSuchAlgorithmException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.ALGORITHM_CANNOT_BE_FOUND);
        }

        return new EncryptionServiceImpl(alias, pk, key);
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
                throw SeedException.createNew(CryptoErrorCodes.NO_KEYSTORE_CONFIGURED);
            }
        }

        if (certificate != null) {
            return certificate.getPublicKey();
        }

        return null;
    }

    private String getCertificateLocation(String alias) {
        String certLocation;

        // Find the certificate location from the classpath
        String certResource = getCertificateLocation(alias, CERT_RESOURCE);
        if (certResource != null) {
            URL urlResource = SeedReflectionUtils.findMostCompleteClassLoader().getResource(certResource);
            if (urlResource == null) {
                throw SeedException.createNew(CryptoErrorCodes.CERTIFICATE_NOT_FOUND)
                        .put("alias", alias).put("certResource", certResource);
            }
            certLocation = urlResource.getFile();
        } else {
            // Otherwise get the file path from the configuration
            certLocation = getCertificateLocation(alias, CERT_FILE);
        }
        return certLocation;
    }

    private Certificate loadCertificateFromFile(String certLocation) {
        Certificate certificate = null;
        if (certLocation != null) {
            FileInputStream in;
            try {
                in = new FileInputStream(certLocation);
            } catch (FileNotFoundException e) {
                throw SeedException.wrap(e, CryptoErrorCodes.ENABLE_TO_READ_CERTIFICATE);
            }
            try {
                certificate = CertificateFactory.getInstance(DEFAULT_CERTIFICATE_TYPE).generateCertificate(in);
            } catch (Exception e) {
                throw SeedException.wrap(e, CryptoErrorCodes.ENABLE_TO_READ_CERTIFICATE);
            }
            try {
                in.close();
            } catch (IOException e) {
                throw SeedException.wrap(e, CryptoErrorCodes.ENABLE_TO_READ_CERTIFICATE);
            }
        }
        return certificate;
    }

    @Nullable
    private String getCertificateLocation(String alias, String type) {
        String locationKey = ConfigurationUtils.buildKey(CERT, alias, type);

        if (configuration.containsKey(locationKey)) {
            return configuration.getString(locationKey);
        }
        return null;
    }
}
