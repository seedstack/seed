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

import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.x509.CertAndKeyGen;
import sun.security.x509.X500Name;

import javax.net.ssl.*;
import java.io.FileOutputStream;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * This class allows to initialize various classes from the Java Cryptography Architecture.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
class SsLLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(SsLLoader.class);
    public static final String SSL = "ssl";

    /**
     * Gets the {@link javax.net.ssl.KeyManager}s from the ssl KeyStore.
     *
     * @return an array of KeyManagers
     */
    KeyManager[] getKeyManagers(KeyStore keyStore, char[] password) {
        KeyManagerFactory keyManagerFactory;
        try {
            keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.ALGORITHM_CANNOT_BE_FOUND);
        }

        try {
            keyManagerFactory.init(keyStore, password);
            return keyManagerFactory.getKeyManagers();
        } catch (UnrecoverableKeyException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.UNRECOVERABLE_KEY);
        } catch (NoSuchAlgorithmException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.ALGORITHM_CANNOT_BE_FOUND);
        } catch (KeyStoreException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.UNEXPECTED_EXCEPTION);
        }
    }

    /**
     * Gets the {@link javax.net.ssl.TrustManager}s from the ssl TrustStore.
     *
     * @return an array of KeyManagers
     */
    TrustManager[] getTrustManager(KeyStore trustStore) {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            return trustManagerFactory.getTrustManagers();
        } catch (NoSuchAlgorithmException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.ALGORITHM_CANNOT_BE_FOUND);
        } catch (KeyStoreException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.UNEXPECTED_EXCEPTION);
        }
    }

    /**
     * Gets an SSLContext configured and initialized.
     *
     * <p>If no keyStore is configured, a default keyStore will be generated.
     * <b>The generated keyStore is not intended to be used in production !</b>
     * It won't work on JRE which don't include sun.* packages like the IBM JRE.
     * </p>
     *
     * @return SSLContext
     */
    SSLContext getSSLContext(String protocol, KeyManager[] keyManagers, TrustManager[] trustManagers) {
        try {
            SSLContext sslContext = SSLContext.getInstance(protocol);
            sslContext.init(keyManagers, trustManagers, null);
            return sslContext;

        } catch (NoSuchAlgorithmException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.ALGORITHM_CANNOT_BE_FOUND);
        } catch (Exception e) {
            throw SeedException.wrap(e, CryptoErrorCodes.UNEXPECTED_EXCEPTION);
        }
    }

    /**
     * Generates a default keyStore with a self signed certificate.
     * <p>
     * <b>It won't work on JRE which don't include sun.security.* classes.</b>
     * </p>
     *
     * @return keyManager array
     */
    @IgnoreJRERequirement
    // animal-sniffer anno: Ignore the usage of sun.* classes
    KeyStore generateKeyStore(String keyStoreLocation, String alias, String password) {
        try {
            SeedReflectionUtils.findMostCompleteClassLoader().loadClass("sun.security.x509.CertAndKeyGen");
        } catch (ClassNotFoundException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.ENABLE_TO_GENERATE_SSL_CERTIFICATE);
        }

        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null);

            // Generate the key pair
            CertAndKeyGen keypair = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
            keypair.generate(2048);

            // Get a self signed certificate
            X500Name x500Name = new X500Name("localhost", "", "", "", "", "");
            int validity = 7; // days
            X509Certificate selfCertificate = keypair.getSelfCertificate(x500Name, new Date(), (long) validity * 24 * 60 * 60);
            X509Certificate[] chain = new X509Certificate[1];
            chain[0] = selfCertificate;

            // Get the private key
            PrivateKey privKey = keypair.getPrivateKey();

            // Add the key pair in the KeyStore
            char[] keyPass = password.toCharArray();
            keyStore.setKeyEntry(alias, privKey, keyPass, chain);

            // Write the KeyStore to its location
            keyStore.store(new FileOutputStream(keyStoreLocation), keyPass);
            LOGGER.warn("Using auto generated self-signed certificate. NOT SAFE FOR PROD !");

            return keyStore;

        } catch (Exception e) {
            throw SeedException.wrap(e, CryptoErrorCodes.UNABLE_TO_GENERATE_SELF_CERTIFICATE);
        }
    }
}
