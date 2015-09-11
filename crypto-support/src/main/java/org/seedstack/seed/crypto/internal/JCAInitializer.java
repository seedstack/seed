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

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class JCAInitializer {

    private final JCADefinitionFactory jcaDefinitionFactory = new JCADefinitionFactory();

    /**
     * Gets the master keyStore.
     *
     * The master keyStore is loaded using the configuration for the seed-bootstrap configuration.
     *
     * @return the {@link KeyStoreDefinition}
     */
    public KeyStore getMasterKeyStore() {
        return loadKeyStore(jcaDefinitionFactory.getMasterKeyStoreDefinition());
    }

    /**
     * Gets the {@link javax.net.ssl.KeyManager}s from the ssl KeyStore.
     *
     * @return an array of KeyManagers
     */
    public KeyManager[] getKeyManagers() {
        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            KeyStore keyStore = loadKeyStore(jcaDefinitionFactory.getSslKeyStoreDefinition());
            char[] keyStorePassword = jcaDefinitionFactory.getSslKeyStoreDefinition().getPassword().toCharArray();
            keyManagerFactory.init(keyStore, keyStorePassword);
            return keyManagerFactory.getKeyManagers();
        } catch (Exception e) {
            // TODO throw SeedException
            throw new IllegalStateException(e);
        }
    }

    /**
     * Gets the {@link javax.net.ssl.TrustManager}s from the ssl TrustStore.
     *
     * @return an array of KeyManagers
     */
    public TrustManager[] getTrustManager() {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore trustStore = loadKeyStore(jcaDefinitionFactory.getSslTrustStoreDefinition());
            trustManagerFactory.init(trustStore);
            return  trustManagerFactory.getTrustManagers();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    KeyStore loadKeyStore(KeyStoreDefinition keyStoreDefinition) {
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
