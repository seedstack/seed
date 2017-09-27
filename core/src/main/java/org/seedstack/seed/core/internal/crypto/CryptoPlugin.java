/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.crypto;

import com.google.common.base.Strings;
import com.google.inject.Key;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.seed.crypto.EncryptionService;
import org.seedstack.seed.crypto.spi.SSLProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoPlugin extends AbstractSeedPlugin implements SSLProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoPlugin.class);
    private static final String ALIAS = "alias";

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
        encryptionServices.putAll(
                new EncryptionServiceBindingFactory().createBindings(cryptoConfig, this.keyPairConfigs, keyStores));
        LOGGER.debug("Registered {} cryptographic key(s)", encryptionServices.size());

        // init SSL context if possible
        this.sslConfig = cryptoConfig.ssl();
        this.sslContext = configureSSL(this.sslConfig).orElse(null);

        return InitState.INITIALIZED;
    }

    private Optional<SSLContext> configureSSL(CryptoConfig.SSLConfig sslConfig) {
        SSLLoader sslLoader = new SSLLoader();

        TrustManager[] trustManagers;
        KeyStore trustStore = keyStores.get(sslConfig.getTrustStore());
        if (trustStore == null) {
            return Optional.empty();
        } else {
            trustManagers = sslLoader.getTrustManager(trustStore);
        }

        return configureKeyManagers(sslConfig).map(
                keyManagers1 -> sslLoader.getSSLContext(this.sslConfig.getProtocol(), keyManagers1, trustManagers));
    }

    private Optional<KeyManager[]> configureKeyManagers(CryptoConfig.SSLConfig sslConfig) {
        String keyStoreName = sslConfig.getKeyStore();
        CryptoConfig.KeyStoreConfig keyStoreConfig = keyStoreConfigs.get(keyStoreName);

        if (keyStoreConfig == null) {
            return Optional.empty();
        }

        String aliasName = sslConfig.getAlias();
        CryptoConfig.KeyStoreConfig.AliasConfig aliasConfig = keyStoreConfig.getAliases().get(aliasName);
        if (aliasConfig == null) {
            return Optional.empty();
        }
        if (Strings.isNullOrEmpty(aliasConfig.getPassword())) {
            throw SeedException.createNew(CryptoErrorCode.MISSING_ALIAS_PASSWORD)
                    .put(ALIAS, aliasName)
                    .put("ksName", keyStoreName);
        }

        return Optional.of(
                new SSLLoader().getKeyManagers(keyStores.get(keyStoreName), aliasConfig.getPassword().toCharArray()));
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
