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

import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.crypto.api.EncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.security.KeyStore;
import java.util.*;

public class CryptoPlugin extends AbstractPlugin {

    public static final String CRYPTO_PLUGIN_PREFIX = "org.seedstack.seed.crypto";
    public static final String MASTER_KEYSTORE_NAME = "master";
    public static final String MASTER_KEY_NAME = "seed";
    public static final String DEFAULT_KEY_NAME = "default";
    public static final String MASTER_KEYSTORE = "keystore." + MASTER_KEYSTORE_NAME + ".path";


    public static final String GENERATED_PASSWORD = "changeit";
    public static final String GENERATED_ALIAS = "undertow";

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoPlugin.class);
    public static final String SSL = "ssl";
    public static final String GENERATE_KEYSTORE = "generate-keystore";

    private final Map<String, EncryptionService> encryptionServices = new HashMap<String, EncryptionService>();
    private final Map<String, KeyStore> keyStores = new HashMap<String, KeyStore>();
    private final Map<String, KeyStoreConfig> keyStoreConfigs = new HashMap<String, KeyStoreConfig>();

    private SslConfig sslConfig;
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
        Configuration cryptoConfig = application.getConfiguration().subset(CRYPTO_PLUGIN_PREFIX);

        // init encryption services and KeyStores
        keyStoreConfigs.putAll(configureKeyStores(cryptoConfig));
        keyStores.putAll(registerKeyStores(keyStoreConfigs));
        encryptionServices.putAll(registerEncryptionService(application, keyStoreConfigs, keyStores));

        LOGGER.debug("Registered {} cryptographic key(s)", encryptionServices.size());

        // init SSL context (if a KeyStore is specified or if it should be generated)
        Configuration sslConfiguration = cryptoConfig.subset(SSL);
        if (sslConfiguration.containsKey("keystore") || autoGenerateSSLKeyStore(sslConfiguration)) {
            configureSSL(application, sslConfiguration);
        }

        return InitState.INITIALIZED;
    }

    private void configureSSL(Application application, Configuration sslConfiguration) {
        SsLLoader ssLLoader = new SsLLoader();

        KeyManager[] keyManagers = configureKeyManagers(application, sslConfiguration);

        TrustManager[] trustManagers = null;
        if (sslConfiguration.containsKey("truststore")) {
            String trustStoreName = sslConfiguration.getString("truststore");
            KeyStore trustStore = keyStores.get(trustStoreName);
            if (trustStore == null) {
                throw new PluginException("No configuration found for the keystore named: " + trustStoreName);
            }
            trustManagers = ssLLoader.getTrustManager(trustStore);
        }

        sslConfig = new SslConfigFactory().createSslConfig(sslConfiguration);
        sslContext = ssLLoader.getSSLContext(sslConfig.getProtocol(), keyManagers, trustManagers);
    }

    private KeyManager[] configureKeyManagers(Application application, Configuration sslConfiguration) {
        SsLLoader ssLLoader = new SsLLoader();

        KeyStore keyStore;
        String password;

        if (sslConfiguration.containsKey("keystore")) {
            String keyStoreName = sslConfiguration.getString("keystore");
            keyStore = keyStores.get(keyStoreName);
            if (keyStore == null) {
                throw new PluginException("No configuration found for the keystore named: " + keyStoreName);
            }
            String alias;
            if (sslConfiguration.containsKey("alias")) {
                alias = sslConfiguration.getString("alias");
            } else {
                alias = SSL;
            }

            KeyStoreConfig keyStoreConfig = keyStoreConfigs.get(keyStoreName);
            password = keyStoreConfig.getAliasPasswords().get(alias);
            if (password == null || "".equals(password)) {
                throw new PluginException("Missing password for the alias \"" + alias + "\" in the KeyStore \"" + keyStoreName + "\"");
            }
        } else if (autoGenerateSSLKeyStore(sslConfiguration)) {
            File sslDir = application.getStorageLocation(SSL);
            String keyStoreLocation = sslDir.getPath() + File.separator + "ssl.keystore";
            File keyStoreFile = new File(keyStoreLocation);
            if (keyStoreFile.canRead()) {
                keyStore = new KeyStoreLoader().load("ssl", keyStoreLocation, GENERATED_PASSWORD, null, null);
            } else {
                keyStore = ssLLoader.generateKeyStore(keyStoreLocation, GENERATED_ALIAS, GENERATED_PASSWORD);
            }
            password = GENERATED_PASSWORD;
        } else {
            throw new PluginException("Missing configuration for the SSL KeyStore");
        }

        return ssLLoader.getKeyManagers(keyStore, password.toCharArray());
    }

    private boolean autoGenerateSSLKeyStore(Configuration sslConfiguration) {
        return sslConfiguration.getBoolean(GENERATE_KEYSTORE, false);
    }


    private Map<String, KeyStoreConfig> configureKeyStores(Configuration cryptoConfig) {
        final Map<String, KeyStoreConfig> keyStoreConfigs = new HashMap<String, KeyStoreConfig>();
        KeyStoreConfigFactory keyStoreConfigFactory = new KeyStoreConfigFactory(cryptoConfig);

        String[] strings = cryptoConfig.getStringArray("keystores");
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

    private Map<String, EncryptionService> registerEncryptionService(Application application, Map<String, KeyStoreConfig> keyStoreConfigs, Map<String, KeyStore> keyStores) {
        Map<String, EncryptionService> encryptionServices = new HashMap<String, EncryptionService>();
        for (Map.Entry<String, KeyStoreConfig> entry : keyStoreConfigs.entrySet()) {
            KeyStoreConfig keyStoreConfig = entry.getValue();
            String keyStoreName = entry.getKey();
            EncryptionServiceFactory serviceFactory = new EncryptionServiceFactory(application, keyStores.get(keyStoreName));
            for (Map.Entry<String, String> aliasPasswordEntry : keyStoreConfig.getAliasPasswords().entrySet()) {
                EncryptionService encryptionService = serviceFactory.create(aliasPasswordEntry.getKey(), aliasPasswordEntry.getValue().toCharArray());
                encryptionServices.put(aliasPasswordEntry.getKey(), encryptionService);
            }
        }
        return encryptionServices;
    }

    /**
     * Provides an {@link javax.net.ssl.SSLContext} configured during the init phase.
     *
     * @return an SSL context, or null before the init phase
     */
    public SSLContext getSslContext() {
        return sslContext;
    }

    /**
     * Provides the {@link org.seedstack.seed.crypto.internal.SslConfig} after the init phase.
     *
     * @return the SSL configuration, or null before the init phase
     */
    public SslConfig getSslConfig() {
        return sslConfig;
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        return plugins;
    }
}
