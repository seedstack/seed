/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import com.google.inject.Key;
import io.nuun.kernel.api.Plugin;
import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.Application;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.core.utils.ConfigurationUtils;
import org.seedstack.seed.crypto.EncryptionService;
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
    public static final String PASSWORD = "password";
    public static final String CERT = "cert";
    public static final String CERT_FILE = "file";
    public static final String CERT_RESOURCE = "resource";
    public static final String QUALIFIER = "qualifier";

    private final Map<Key<EncryptionService>, EncryptionService> encryptionServices = new HashMap<Key<EncryptionService>, EncryptionService>();
    private final Map<String, KeyStore> keyStores = new HashMap<String, KeyStore>();
    private final Map<String, KeyStoreConfig> keyStoreConfigs = new HashMap<String, KeyStoreConfig>();
    private final List<KeyPairConfig> keyPairConfigs = new ArrayList<KeyPairConfig>();

    private SSLConfiguration sslConfiguration;
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
    public InitState init(InitContext initContext) {
        Application application = initContext.dependency(ApplicationPlugin.class).getApplication();
        Configuration cryptoConfig = application.getConfiguration().subset(CONFIG_PREFIX);

        // Retrieve key store configurations
        this.keyStoreConfigs.putAll(getKeyStoreConfigs(cryptoConfig));

        // Load key stores
        KeyStoreLoader keyStoreLoader = new KeyStoreLoader();
        for (Map.Entry<String, KeyStoreConfig> entry : keyStoreConfigs.entrySet()) {
            keyStores.put(entry.getKey(), keyStoreLoader.load(entry.getValue()));
        }

        // Retrieve key pair configurations
        KeyPairConfigFactory keyPairConfigFactory = new KeyPairConfigFactory(cryptoConfig);
        for (Map.Entry<String, KeyStore> entry : keyStores.entrySet()) {
            this.keyPairConfigs.addAll(keyPairConfigFactory.create(entry.getKey(), entry.getValue()));
        }

        // Prepare encryption service bindings
        encryptionServices.putAll(new EncryptionServiceBindingFactory().createBindings(cryptoConfig, this.keyPairConfigs, keyStores));
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

    private Map<String, KeyStoreConfig> getKeyStoreConfigs(Configuration cryptoConfig) {
        final Map<String, KeyStoreConfig> keyStoreConfigs = new HashMap<String, KeyStoreConfig>();
        KeyStoreConfigFactory keyStoreConfigFactory = new KeyStoreConfigFactory(cryptoConfig);

        for (String keyStoreName : getKeyStoreNames(cryptoConfig, keyStoreConfigFactory)) {
            keyStoreConfigs.put(keyStoreName, keyStoreConfigFactory.create(keyStoreName));
        }
        return keyStoreConfigs;
    }

    private List<String> getKeyStoreNames(Configuration cryptoConfig, KeyStoreConfigFactory keyStoreConfigFactory) {
        String[] strings = cryptoConfig.getStringArray(KEYSTORES);
        List<String> keyStoreNames = new ArrayList<String>();
        keyStoreNames.addAll(Arrays.asList(strings));
        if (keyStoreConfigFactory.isKeyStoreConfigured(MASTER_KEYSTORE_NAME)) {
            keyStoreNames.add(MASTER_KEYSTORE_NAME);
        }
        if (keyStoreConfigFactory.isKeyStoreConfigured(DEFAULT_KEY_NAME)) {
            keyStoreNames.add(DEFAULT_KEY_NAME);
        }
        return keyStoreNames;
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
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(ApplicationPlugin.class);
    }
}
