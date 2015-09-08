/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.internal.application.SeedConfigLoader;
import org.seedstack.seed.core.utils.SeedReflectionUtils;

import javax.security.cert.X509Certificate;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

/**
 * This factory allows to create the definitions of JCA objects from the Seed configuration.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class JCADefinitionFactory {

    public static final String PATH = "path";
    public static final String PASSWORD = "password";
    public static final String ALIAS = "alias";
    public static final String TYPE = "type";
    public static final String PROVIDER = "provider";
    public static final String KEY_PASSWORD = "password";
    public static final String CERT_RESOURCE = "cert.resource";
    public static final String CERT_FILE = "cert.file";

    public static final String SSL = "ssl";

    public static enum KeyStoreMode {
        KEYSTORE, TRUSTSTORE
    }

    /**
     * Gets the master keyStore definition.
     *
     * The master keyStore is loaded using the seed-bootstrap configuration.
     *
     * @return the {@link KeyStoreDefinition}, or null if the master keyStore was not defined
     */
    public KeyStoreDefinition getMasterKeyStoreDefinition() {
        return createKeyStoreDefinition(new SeedConfigLoader().bootstrapConfig(), "");
    }

    /**
     * Gets the master key definition.
     *
     * The master key is loaded using the seed-bootstrap configuration.
     *
     * @return the {@link KeyStoreDefinition} or null if not defined
     */
    public KeyDefinition getMasterKeyDefinition() {
        return createKeyDefinition(new SeedConfigLoader().bootstrapConfig().subset(CryptoPlugin.MASTER_KEY_NAME), CryptoPlugin.MASTER_KEY_NAME, null);
    }

    /**
     * Gets the master keyStore definition.
     *
     * The master keyStore is loaded using the seed-bootstrap configuration.
     *
     * @return the {@link KeyStoreDefinition}, or null if the master keyStore was not defined
     */
    public KeyStoreDefinition getSslKeyStoreDefinition() {
        return createKeyStoreDefinition(new SeedConfigLoader().bootstrapConfig(), SSL);
    }

    /**
     * Gets the master keyStore definition.
     *
     * The master keyStore is loaded using the seed-bootstrap configuration.
     *
     * @return the {@link KeyStoreDefinition}, or null if the master keyStore was not defined
     */
    public KeyStoreDefinition getSslTrustStoreDefinition() {
        return createKeyStoreDefinition(new SeedConfigLoader().bootstrapConfig(), SSL, KeyStoreMode.TRUSTSTORE);
    }

    /**
     * Creates a {@link KeyStoreDefinition}. This definition is used to create a Key Store.
     *
     * @param configuration the configuration containing key store properties
     * @param keyName       the key name to use
     * @return the {@link KeyStoreDefinition}
     */
    KeyStoreDefinition createKeyStoreDefinition(Configuration configuration, String keyName) {
        return createKeyStoreDefinition(configuration, keyName, KeyStoreMode.KEYSTORE);
    }

    /**
     * Creates a {@link KeyStoreDefinition}. This definition is used to create a Key Store.
     *
     * @param keyConfig the configuration containing key store properties
     * @param keyName   the key name to use
     * @return the {@link KeyStoreDefinition}, or null
     */
    KeyStoreDefinition createKeyStoreDefinition(Configuration keyConfig, String keyName, KeyStoreMode mode) {
        KeyStoreDefinition keyStoreDefinition;

        if (keyConfig.containsKey(ksConfig(mode, PATH)) && keyConfig.containsKey(ksConfig(mode, PASSWORD))) {
            // Check for a customized keyStore for this definition
            String keyStorePath = keyConfig.getString(ksConfig(mode, PATH));
            String keyStorePassword = keyConfig.getString(ksConfig(mode, PASSWORD));
            String keyStoreType = keyConfig.getString(ksConfig(mode, TYPE));
            String keyStoreProvider = keyConfig.getString(ksConfig(mode, PROVIDER));

            keyStoreDefinition = new KeyStoreDefinition(keyStorePath, keyStorePassword, keyStoreType, keyStoreProvider);
        } else if (keyConfig.containsKey(ksConfig(mode, PATH)) || keyConfig.containsKey(ksConfig(mode, PASSWORD))) {
            // Illegal configuration: path or password are missing
            // TODO
            throw new IllegalStateException("Configuration error: the keyStore for the key " + keyName + " should have both the keyStore path and password defined.");
        } else {
            keyStoreDefinition = null;
        }

        return keyStoreDefinition;
    }

    String ksConfig(KeyStoreMode mode, String key) {
        return mode.toString().toLowerCase() + "." + key;
    }

    /**
     * Creates {@link KeyDefinition}. This definition is used to create and check a {@link X509Certificate}.
     *
     * @param keyConfig properties to define {@link KeyDefinition}.
     * @param keyName   the name of the key.
     * @return the {@link KeyDefinition}.
     */
    KeyDefinition createKeyDefinition(Configuration keyConfig, String keyName, KeyStoreDefinition defaultKS) {
        KeyDefinition definition = new KeyDefinition();
        String resource = keyConfig.getString(CERT_RESOURCE);
        String certLocation;

        if (resource != null) {
            URL urlResource = SeedReflectionUtils.findMostCompleteClassLoader().getResource(resource);
            if (urlResource == null) {
                throw new RuntimeException("Certificate [" + resource + "] not found !");
            }
            certLocation = urlResource.getFile();
        } else {
            certLocation = keyConfig.getString(CERT_FILE);
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

        // Private key information
        definition.setAlias(keyConfig.getString(ALIAS));
        definition.setPassword(keyConfig.getString(KEY_PASSWORD));

        definition.setKeyStoreDefinition(resolveKeyStoreForKey(keyConfig, keyName, defaultKS));
        return definition;
    }

    private KeyStoreDefinition resolveKeyStoreForKey(Configuration keyConfig, String keyName, KeyStoreDefinition defaultKS) {
        KeyStoreDefinition keyStoreDefinition = null;
        // look for override KeyStore (except for the master-key)
        if (!keyName.equals(CryptoPlugin.MASTER_KEY_NAME)) {
            keyStoreDefinition = createKeyStoreDefinition(keyConfig, keyName, KeyStoreMode.KEYSTORE);
        }

        // Otherwise fallback on the default KeyStore
        if (keyStoreDefinition == null) {
            keyStoreDefinition = defaultKS;
        }

        // Finally fallback on the master KeyStore
        if (keyStoreDefinition == null) {
            keyStoreDefinition = getMasterKeyStoreDefinition();
        }

        return keyStoreDefinition;
    }
}
