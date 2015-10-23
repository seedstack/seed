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
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.crypto.api.EncryptionService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

/**
 * Factory to create a new {@link EncryptionService} object. This factory checks the KeyStore state (connection) if a KeyStore is used.
 *
 * @author thierry.bouvet@mpsa.com
 */
public class EncryptionServiceFactory {

    private static final String CERT_CONFIG = CryptoPlugin.CRYPTO_PLUGIN_PREFIX + ".cert";
    private static final String CERT_FILE = "file";
    private static final String CERT_RESOURCE = "resource";

    private final Configuration configuration;
    private final KeyStore keyStore;

    /**
     * Constructs an encryption service factory for a specific KeyStore.
     *
     * @param application the application, used to find external certificate locations
     * @param keyStore    the KeyStore which holds the key pairs
     */
    public EncryptionServiceFactory(Application application, KeyStore keyStore) {
        this.configuration = application.getConfiguration();
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
    public EncryptionService create(String alias, char[] password) {
        PublicKey pk = getPublicKey(alias);

        if (this.keyStore == null) {
            throw new IllegalArgumentException("No keystore configured so decrypt is not possible.");
        }
        Key key;
        try {
            key = this.keyStore.getKey(alias, password);
        } catch (UnrecoverableKeyException e) {
            throw new IllegalArgumentException(e);
        } catch (KeyStoreException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }

        return new EncryptionServiceImpl(alias, pk, key);
    }

    private PublicKey getPublicKey(String alias) {
        Certificate certificate = null;

        // Look for an external certificate
        String certLocation = getCertificateLocation(alias);
        if (certLocation != null) {
            certificate = loadCertificateFromFile(certLocation);
        } else {
            // Otherwise load the certificate from the key store
            try {
                certificate = keyStore.getCertificate(alias);
            } catch (KeyStoreException e) {
                e.printStackTrace();
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
                throw new RuntimeException("Certificate [" + alias + "] not found !");
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
                throw new RuntimeException("Certificate [" + certLocation + "] not found !");
            }
            try {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                certificate = certificateFactory.generateCertificate(in);
            } catch (Exception e) {
                throw new RuntimeException("Certificate [" + certLocation + "] parsing error !");
            }
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException("Certificate [" + certLocation + "] not closed !");
            }
        }
        return certificate;
    }

    private String getCertificateLocation(String alias, String type) {
        String locationKey = CERT_CONFIG + alias + type;

        if (configuration.containsKey(locationKey)) {
            return configuration.getString(locationKey);
        }
        return null;
    }
}
