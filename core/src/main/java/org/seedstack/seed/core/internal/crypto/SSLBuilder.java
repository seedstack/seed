/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.crypto;

import com.google.common.base.Strings;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.CryptoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SSLBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SSLBuilder.class);
    private final KeyStore trustStore;
    private final Map<String, KeyStore> keyStores;
    private SSLContext sslContext = null;
    private List<KeyManagerAdapter> keyManagerAdapters = new ArrayList<>();

    SSLBuilder(KeyStore trustStore, Map<String, KeyStore> keyStores) {
        this.trustStore = trustStore;
        this.keyStores = keyStores;
    }

    synchronized SSLContext getSSLContext(CryptoConfig cryptoConfig) {
        if (sslContext == null) {
            CryptoConfig.SSLConfig sslConfig = cryptoConfig.ssl();

            // Trust
            TrustManager[] trustManagers;
            if (trustStore != null) {
                LOGGER.info("Using the configured truststore for SSL");
                trustManagers = getTrustManagers(sslConfig, trustStore);
            } else {
                LOGGER.info("No truststore configured, platform default will be used for SSL");
                trustManagers = null;
            }

            // Keys
            KeyManager[] keyManagers;
            if (cryptoConfig.keyStores().containsKey(sslConfig.getKeystore())) {
                LOGGER.info("Keystore '{}' will be used for SSL X509 certificates", sslConfig.getKeystore());
                keyManagers = getKeyManagers(sslConfig);
            } else {
                LOGGER.debug("Keystore '{}' is not configured, platform default will be used for SSL",
                        sslConfig.getKeystore());
                keyManagers = null;
            }

            try {
                sslContext = SSLContext.getInstance(sslConfig.getProtocol());
                String randomAlgorithm = sslConfig.getRandomAlgorithm();
                if (Strings.isNullOrEmpty(randomAlgorithm)) {
                    sslContext.init(keyManagers, trustManagers, null);
                } else {
                    sslContext.init(keyManagers, trustManagers, SecureRandom.getInstance(randomAlgorithm));
                }
            } catch (NoSuchAlgorithmException e) {
                throw SeedException.wrap(e, CryptoErrorCode.ALGORITHM_CANNOT_BE_FOUND);
            } catch (Exception e) {
                throw SeedException.wrap(e, CryptoErrorCode.UNEXPECTED_EXCEPTION);
            }
        }
        return sslContext;
    }

    synchronized List<KeyManagerAdapter> getKeyManagerAdapters() {
        return Collections.unmodifiableList(keyManagerAdapters);
    }

    private KeyManager[] getKeyManagers(CryptoConfig.SSLConfig sslConfig) {
        KeyManagerFactory keyManagerFactory;
        try {
            String keyManagerAlgorithm = sslConfig.getKeyManagerAlgorithm();
            if (Strings.isNullOrEmpty(keyManagerAlgorithm)) {
                keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            } else {
                keyManagerFactory = KeyManagerFactory.getInstance(keyManagerAlgorithm);
            }
        } catch (NoSuchAlgorithmException e) {
            throw SeedException.wrap(e, CryptoErrorCode.ALGORITHM_CANNOT_BE_FOUND);
        }

        try {
            KeyStore keyStore = keyStores.get(sslConfig.getKeystore());
            if (keyStore == null) {
                throw SeedException.createNew(CryptoErrorCode.KEYSTORE_CONFIGURATION_ERROR)
                        .put("ksName", sslConfig.getKeystore());
            }
            keyManagerFactory.init(keyStore,
                    Optional.ofNullable(sslConfig.getKeyPassword()).map(String::toCharArray).orElse(null));
            return Arrays.stream(keyManagerFactory.getKeyManagers())
                    .map(km -> {
                        if (km instanceof X509KeyManager) {
                            KeyManagerAdapter keyManagerAdapter = new KeyManagerAdapter(((X509KeyManager) km));
                            keyManagerAdapters.add(keyManagerAdapter);
                            return keyManagerAdapter;
                        } else {
                            return km;
                        }
                    })
                    .toArray(KeyManager[]::new);
        } catch (UnrecoverableKeyException e) {
            throw SeedException.wrap(e, CryptoErrorCode.UNRECOVERABLE_KEY);
        } catch (NoSuchAlgorithmException e) {
            throw SeedException.wrap(e, CryptoErrorCode.ALGORITHM_CANNOT_BE_FOUND);
        } catch (KeyStoreException e) {
            throw SeedException.wrap(e, CryptoErrorCode.UNEXPECTED_EXCEPTION);
        }
    }

    private TrustManager[] getTrustManagers(CryptoConfig.SSLConfig sslConfig,
            KeyStore trustStore) {
        try {
            TrustManagerFactory trustManagerFactory;
            String trustManagerAlgorithm = sslConfig.getTrustManagerAlgorithm();
            if (Strings.isNullOrEmpty(trustManagerAlgorithm)) {
                trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            } else {
                trustManagerFactory = TrustManagerFactory.getInstance(trustManagerAlgorithm);
            }
            trustManagerFactory.init(trustStore);
            return trustManagerFactory.getTrustManagers();
        } catch (NoSuchAlgorithmException e) {
            throw SeedException.wrap(e, CryptoErrorCode.ALGORITHM_CANNOT_BE_FOUND);
        } catch (KeyStoreException e) {
            throw SeedException.wrap(e, CryptoErrorCode.UNEXPECTED_EXCEPTION);
        }
    }
}
