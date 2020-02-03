/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
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
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509KeyManager;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.seed.crypto.EncryptionService;
import org.seedstack.seed.crypto.spi.SSLProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoPlugin extends AbstractSeedPlugin implements SSLProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoPlugin.class);
    private final Map<Key<EncryptionService>, EncryptionService> encryptionServices = new HashMap<>();
    private final Map<String, KeyStore> keyStores = new HashMap<>();
    private KeyStore trustStore;
    private SSLContext sslContext;
    private List<KeyManagerAdapter> keyManagerAdapters;
    private Class<? extends X509KeyManager> keyManagerClass;

    @Override
    public String name() {
        return "crypto";
    }

    @Override
    public InitState initialize(InitContext initContext) {
        CryptoConfig cryptoConfig = getConfiguration(CryptoConfig.class);
        KeyStoreLoader keyStoreLoader = new KeyStoreLoader();
        this.trustStore = loadTrustStore(cryptoConfig, keyStoreLoader);
        this.keyStores.putAll(loadKeyStores(cryptoConfig, keyStoreLoader));
        this.encryptionServices.putAll(configEncryptionServices(cryptoConfig));
        SSLBuilder sslBuilder = new SSLBuilder(trustStore, keyStores);
        this.sslContext = sslBuilder.getSSLContext(cryptoConfig);
        this.keyManagerAdapters = sslBuilder.getKeyManagerAdapters();
        this.keyManagerClass = cryptoConfig.ssl().getX509KeyManager();
        return InitState.INITIALIZED;
    }

    private KeyStore loadTrustStore(CryptoConfig cryptoConfig, KeyStoreLoader keyStoreLoader) {
        CryptoConfig.StoreConfig trustStoreConfig = cryptoConfig.getTrustStore();
        if (trustStoreConfig == null) {
            return null;
        } else {
            LOGGER.info("Loading truststore from {}", trustStoreConfig.getPath());
            return keyStoreLoader.load("<truststore>", trustStoreConfig);
        }
    }

    private Map<String, KeyStore> loadKeyStores(CryptoConfig cryptoConfig, KeyStoreLoader keyStoreLoader) {
        Map<String, KeyStore> loadedKeyStores = new HashMap<>();
        cryptoConfig.keyStores().entrySet().stream()
                .peek(e -> LOGGER.info("Loading keystore '{}' from {}", e.getKey(), e.getValue().getPath()))
                .forEach(e -> loadedKeyStores.put(e.getKey(), keyStoreLoader.load(e.getKey(), e.getValue())));
        return loadedKeyStores;
    }

    private Map<Key<EncryptionService>, EncryptionService> configEncryptionServices(CryptoConfig cryptoConfig) {
        List<KeyPairConfig> keyPairConfigs = new ArrayList<>();

        // Retrieve key pair configurations
        KeyPairConfigFactory keyPairConfigFactory = new KeyPairConfigFactory(cryptoConfig);
        for (Map.Entry<String, KeyStore> entry : keyStores.entrySet()) {
            keyPairConfigFactory.create(entry.getKey(), entry.getValue()).stream()
                    .peek(keyPairConfig -> LOGGER.debug("Encryption service '{}' defined from keystore '{}'",
                            keyPairConfig.getAlias(),
                            entry.getKey()))
                    .forEach(keyPairConfigs::add);
        }

        // Create encryption service bindings
        return new EncryptionServiceBindingFactory().createBindings(keyPairConfigs, keyStores);
    }

    @Override
    public Object nativeUnitModule() {
        return new CryptoModule(encryptionServices,
                keyStores,
                trustStore,
                sslContext,
                keyManagerAdapters,
                keyManagerClass);
    }

    @Override
    public Optional<SSLContext> sslContext() {
        return Optional.ofNullable(sslContext);
    }

    @Override
    public CryptoConfig.SSLConfig sslConfig() {
        return getConfiguration(CryptoConfig.SSLConfig.class);
    }

    static EncryptionService getMasterEncryptionService(EncryptionServiceFactory encryptionServiceFactory,
            CryptoConfig.KeyStoreConfig masterKeyStoreConfig, String alias) {
        CryptoConfig.KeyStoreConfig.AliasConfig aliasConfig = masterKeyStoreConfig.getAliases().get(alias);
        if (aliasConfig == null || Strings.isNullOrEmpty(aliasConfig.getPassword())) {
            throw SeedException.createNew(CryptoErrorCode.MISSING_MASTER_KEY_PASSWORD);
        }
        return encryptionServiceFactory.create(alias,
                aliasConfig.getPassword().toCharArray());
    }
}
