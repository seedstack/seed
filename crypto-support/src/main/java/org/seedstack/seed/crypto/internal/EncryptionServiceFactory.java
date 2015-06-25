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

import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.crypto.api.EncryptionService;

import javax.security.cert.X509Certificate;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Factory to create a new {@link EncryptionService} object. This factory checks the keystore state (connection) if a keystore is used.
 *
 * @author thierry.bouvet@mpsa.com
 */
class EncryptionServiceFactory {
    public static final String KEYSTORE_PATH = "keystore.path";
    public static final String KEYSTORE_PASSWORD = "keystore.password";
    public static final String KEYSTORE_ALIAS = "keystore.alias";
    public static final String KEY_PASSWORD = "key.password";
    public static final String CERT_RESOURCE = "cert.resource";
    public static final String CERT_FILE = "cert.file";

    /**
     * Load and check a certificate and a keystore.
     */
    EncryptionServiceImpl createEncryptionService(KeyStoreDefinition keyStoreDefinition, CertificateDefinition certificateDefinition) {
        return new EncryptionServiceImpl(loadKeystore(keyStoreDefinition), certificateDefinition);
    }

    /**
     * Create {@link CertificateDefinition}. This definition is used to create and check a {@link X509Certificate}.
     *
     * @param configuration properties to define {@link CertificateDefinition}.
     * @param keyName       the name of the key.
     * @return the {@link CertificateDefinition}.
     */
    CertificateDefinition createCertificateDefinition(Configuration configuration, String keyName) {
        Configuration keyConfiguration = configurationForKey(configuration, keyName);
        CertificateDefinition definition = new CertificateDefinition();
        String resource = keyConfiguration.getString(CERT_RESOURCE);
        String certLocation;

        if (resource != null) {
            URL urlResource = SeedReflectionUtils.findMostCompleteClassLoader(null).getResource(resource);
            if (urlResource == null) {
                throw new RuntimeException("Certificate [" + resource + "] not found !");
            }
            certLocation = urlResource.getFile();
        } else {
            certLocation = keyConfiguration.getString(CERT_FILE);
        }

        // Certificate information
        if (certLocation != null) {
            FileInputStream in;
            try {
                in = new FileInputStream(certLocation);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Certificate [" + certLocation + "] not found !");
            }
            try {
                definition.setCertificate(X509Certificate.getInstance(in));
            } catch (javax.security.cert.CertificateException e) {
                throw new RuntimeException("Certificate [" + certLocation + "] parsing error !");
            }
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException("Certificate [" + certLocation + "] not closed !");
            }
        }

        // Private key informations
        definition.setAlias(keyConfiguration.getString(KEYSTORE_ALIAS));
        definition.setPassword(keyConfiguration.getString(KEY_PASSWORD));

        return definition;
    }

    /**
     * Create a {@link KeyStoreDefinition}. This definition is used to create a Key Store.
     *
     * @param configuration the configuration containing key store properties
     * @param keyName       the key name to use
     * @return the {@link KeyStoreDefinition}
     */
    KeyStoreDefinition createKeyStoreDefinition(Configuration configuration, String keyName) {
        String keyStorePath = configuration.getString(KEYSTORE_PATH);
        String keyStorePassword = configuration.getString(KEYSTORE_PASSWORD);

        // Check for a customized keystore for this definition
        Configuration keyConfiguration = configurationForKey(configuration, keyName);
        if (keyConfiguration.containsKey(KEYSTORE_PATH)) {
            keyStorePath = keyConfiguration.getString(KEYSTORE_PATH);
        }
        if (keyConfiguration.containsKey(KEYSTORE_PASSWORD)) {
            keyStorePassword = keyConfiguration.getString(KEYSTORE_PASSWORD);
        }

        return new KeyStoreDefinition(keyStorePath, keyStorePassword);
    }

    private Configuration configurationForKey(Configuration configuration, String keyName) {
        Configuration keyConfiguration = configuration.subset("key." + keyName);
        if (keyConfiguration.isEmpty()) {
            throw new RuntimeException("Key configuration [" + keyName + "] is not defined !");
        }

        return keyConfiguration;
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

        FileInputStream inputStream;
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
