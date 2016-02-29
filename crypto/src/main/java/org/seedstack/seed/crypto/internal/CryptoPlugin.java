/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import com.google.common.base.Strings;
import com.google.inject.Key;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.core.utils.ConfigurationUtils;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.seed.crypto.EncryptionService;
import org.seedstack.seed.crypto.spi.SSLProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CryptoPlugin extends AbstractSeedPlugin implements SSLProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoPlugin.class);

    /* Conventional names */
    public static final String KEYSTORE = "keystore";
    public static final String TRUSTSTORE = "truststore";
    public static final String ALIAS = "alias";
    public static final String SSL = "ssl";

    /* configuration keys */
    public static final String DEFAULT_KEY_NAME = "default";
    public static final String MASTER_KEYSTORE_NAME = "master";
    public static final String MASTER_KEYSTORE_PATH = ConfigurationUtils.buildKey("keystore", MASTER_KEYSTORE_NAME, "path");
    public static final String MASTER_KEY_NAME = "seed";
    public static final String KEYSTORES = "keystores";
    public static final String PASSWORD = "password";
    public static final String CERT = "cert";
    public static final String CERT_FILE = "file";
    public static final String CERT_RESOURCE = "resource";
    public static final String QUALIFIER = "qualifier";

    private final Map<Key<EncryptionService>, EncryptionService> encryptionServices = new HashMap<>();
    private final Map<String, KeyStore> keyStores = new HashMap<>();
    private final Map<String, CryptoConfig.KeyStoreConfig> keyStoreConfigs = new HashMap<>();
    private final List<KeyPairConfig> keyPairConfigs = new ArrayList<>();

    private CryptoConfig.SSLConfig sslConfig;
    private SSLContext sslContext;

    @Override
    public String name() {
        return "crypto";
    }

    @Override
    public Object nativeUnitModule() {
        return new CryptoModule(encryptionServices, keyStores);
    }

    @Override
    public InitState initialize(InitContext initContext) {
        CryptoConfig cryptoConfig = getConfiguration(CryptoConfig.class);

        // Load key stores
        KeyStoreLoader keyStoreLoader = new KeyStoreLoader();
        for (Map.Entry<String, CryptoConfig.KeyStoreConfig> entry : cryptoConfig.keyStores().entrySet()) {
            keyStores.put(entry.getKey(), keyStoreLoader.load(entry.getKey(), entry.getValue()));
            keyStoreConfigs.put(entry.getKey(), entry.getValue());
        }

        // Retrieve key pair configurations
        KeyPairConfigFactory keyPairConfigFactory = new KeyPairConfigFactory(cryptoConfig);
        for (Map.Entry<String, KeyStore> entry : keyStores.entrySet()) {
            this.keyPairConfigs.addAll(keyPairConfigFactory.create(entry.getKey(), entry.getValue()));
        }

        // Prepare encryption service bindings
        encryptionServices.putAll(new EncryptionServiceBindingFactory().createBindings(cryptoConfig, this.keyPairConfigs, keyStores));
        LOGGER.debug("Registered {} cryptographic key(s)", encryptionServices.size());

        // init SSL context if possible
        this.sslConfig = cryptoConfig.ssl();
        this.sslContext = configureSSL(this.sslConfig);

        return InitState.INITIALIZED;
    }

    private SSLContext configureSSL(CryptoConfig.SSLConfig sslConfig) {
        SSLLoader sslLoader = new SSLLoader();

        TrustManager[] trustManagers;
        KeyStore trustStore = keyStores.get(sslConfig.getTrustStore());
        if (trustStore == null) {
            return null;
        } else {
            trustManagers = sslLoader.getTrustManager(trustStore);
        }

        KeyManager[] keyManagers = configureKeyManagers(sslConfig);
        if (keyManagers != null) {
            return sslLoader.getSSLContext(this.sslConfig.getProtocol(), keyManagers, trustManagers);
        } else {
            return null;
        }
    }

    private KeyManager[] configureKeyManagers(CryptoConfig.SSLConfig sslConfig) {
        String keyStoreName = sslConfig.getKeyStore();
        CryptoConfig.KeyStoreConfig keyStoreConfig = keyStoreConfigs.get(keyStoreName);

        if (keyStoreConfig == null) {
            return null;
        }

        String aliasName = sslConfig.getAlias();
        CryptoConfig.KeyStoreConfig.AliasConfig aliasConfig = keyStoreConfig.getAliases().get(aliasName);
        if (aliasConfig == null || Strings.isNullOrEmpty(aliasConfig.getPassword())) {
            throw SeedException.createNew(CryptoErrorCodes.MISSING_ALIAS_PASSWORD)
                    .put(ALIAS, aliasName)
                    .put("ksName", keyStoreName);
        }

        return new SSLLoader().getKeyManagers(keyStores.get(keyStoreName), aliasConfig.getPassword().toCharArray());
    }

    @Override
    public Optional<SSLContext> sslContext() {
        return Optional.ofNullable(sslContext);
    }

    @Override
    public CryptoConfig.SSLConfig sslConfig() {
        return sslConfig;
    }
}
