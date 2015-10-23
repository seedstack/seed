/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import com.google.inject.Key;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.core.utils.ConfigurationUtils;
import org.seedstack.seed.crypto.api.EncryptionService;
import org.seedstack.seed.crypto.spi.SSLProvider;
import org.seedstack.seed.crypto.spi.SSLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyStore;
import java.util.*;

public class CryptoPlugin extends AbstractPlugin implements SSLProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoPlugin.class);

    public static final String CONFIG_PREFIX = "org.seedstack.seed.crypto";

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

    private final Map<Key<EncryptionService>, EncryptionService> encryptionServices = new HashMap<Key<EncryptionService>, EncryptionService>();
    private final Map<String, KeyStore> keyStores = new HashMap<String, KeyStore>();
    private final Map<String, KeyStoreConfig> keyStoreConfigs = new HashMap<String, KeyStoreConfig>();

    private SSLConfiguration sslConfiguration;
    private SSLContext sslContext;

    @Override
    public String name() {
        return "seed-crypto-plugin";
    }

    @Override
    public Object nativeUnitModule() {
        return new CryptoModule(encryptionServices, keyStores);
    }

    @Override
    public InitState init(InitContext initContext) {
        Plugin applicationPlugin = initContext.pluginsRequired().iterator().next();
        if (!(applicationPlugin instanceof ApplicationPlugin)) {
            throw new PluginException("Missing ApplicationPlugin");
        }

        Application application = ((ApplicationPlugin) applicationPlugin).getApplication();
        Configuration cryptoConfig = application.getConfiguration().subset(CONFIG_PREFIX);

        // init encryption services and KeyStores
        keyStoreConfigs.putAll(configureKeyStores(cryptoConfig));
        keyStores.putAll(registerKeyStores(keyStoreConfigs));
        encryptionServices.putAll(new EncryptionServiceBindingFactory().createBindings(cryptoConfig, keyStoreConfigs, keyStores));

        LOGGER.debug("Registered {} cryptographic key(s)", encryptionServices.size());

        // init SSL context (if a KeyStore is specified or if it should be generated)
        Configuration sslConfiguration = cryptoConfig.subset(SSL);
        if (sslConfiguration.containsKey(KEYSTORE)) {
            configureSSL(sslConfiguration);
        }

        return InitState.INITIALIZED;
    }

    private void configureSSL(Configuration sslConfiguration) {
        SSLLoader sslLoader = new SSLLoader();

        KeyManager[] keyManagers = configureKeyManagers(sslConfiguration);

        TrustManager[] trustManagers = null;
        if (sslConfiguration.containsKey(TRUSTSTORE)) {
            String trustStoreName = sslConfiguration.getString(TRUSTSTORE);
            KeyStore trustStore = keyStores.get(trustStoreName);
            if (trustStore == null) {
                throw SeedException.createNew(CryptoErrorCodes.MISSING_SSL_TRUST_STORE_CONFIGURATION);
            }
            trustManagers = sslLoader.getTrustManager(trustStore);
        }

        this.sslConfiguration = new SSLConfigFactory().createSSLConfiguration(sslConfiguration);
        sslContext = sslLoader.getSSLContext(this.sslConfiguration.getProtocol(), keyManagers, trustManagers);
    }

    private KeyManager[] configureKeyManagers(Configuration sslConfiguration) {
        SSLLoader sslLoader = new SSLLoader();

        KeyStore keyStore;
        String password;

        if (sslConfiguration.containsKey(KEYSTORE)) {
            String keyStoreName = sslConfiguration.getString(KEYSTORE);
            keyStore = keyStores.get(keyStoreName);
            if (keyStore == null) {
                throw SeedException.createNew(CryptoErrorCodes.MISSING_SSL_KEY_STORE_CONFIGURATION)
                        .put("ksName", keyStoreName);
            }
            String alias;
            if (sslConfiguration.containsKey(ALIAS)) {
                alias = sslConfiguration.getString(ALIAS);
            } else {
                alias = SSL;
            }

            KeyStoreConfig keyStoreConfig = keyStoreConfigs.get(keyStoreName);
            password = keyStoreConfig.getAliasPasswords().get(alias);
            if (password == null || "".equals(password)) {
                throw SeedException.createNew(CryptoErrorCodes.MISSING_ALIAS_PASSWORD)
                        .put(ALIAS, alias).put("ksName", keyStoreName);
            }
        } else {
            throw SeedException.createNew(CryptoErrorCodes.MISSING_SSL_KEY_STORE_CONFIGURATION);
        }

        return sslLoader.getKeyManagers(keyStore, password.toCharArray());
    }

    private Map<String, KeyStoreConfig> configureKeyStores(Configuration cryptoConfig) {
        final Map<String, KeyStoreConfig> keyStoreConfigs = new HashMap<String, KeyStoreConfig>();
        KeyStoreConfigFactory keyStoreConfigFactory = new KeyStoreConfigFactory(cryptoConfig);

        String[] strings = cryptoConfig.getStringArray(KEYSTORES);
        List<String> keyStoreNames = new ArrayList<String>();
        keyStoreNames.addAll(Arrays.asList(strings));
        if (keyStoreConfigFactory.isKeyStoreConfigured(MASTER_KEYSTORE_NAME)) {
            keyStoreNames.add(MASTER_KEYSTORE_NAME);
        }
        if (keyStoreConfigFactory.isKeyStoreConfigured(DEFAULT_KEY_NAME)) {
            keyStoreNames.add(DEFAULT_KEY_NAME);
        }

        for (String keyStoreName : keyStoreNames) {
            KeyStoreConfig keyStoreConfig = keyStoreConfigFactory.create(keyStoreName);
            keyStoreConfigs.put(keyStoreName, keyStoreConfig);
        }
        return keyStoreConfigs;
    }

    private Map<String, KeyStore> registerKeyStores(Map<String, KeyStoreConfig> keyStoreConfigs) {
        Map<String, KeyStore> keyStores = new HashMap<String, KeyStore>();
        KeyStoreLoader keyStoreLoader = new KeyStoreLoader();
        for (Map.Entry<String, KeyStoreConfig> entry : keyStoreConfigs.entrySet()) {
            keyStores.put(entry.getKey(), keyStoreLoader.load(entry.getValue()));
        }
        return keyStores;
    }

    @Override
    public SSLContext sslContext() {
        return sslContext;
    }

    @Override
    public SSLConfiguration sslConfig() {
        return sslConfiguration;
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        return plugins;
    }
}
