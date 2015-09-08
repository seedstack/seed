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
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.crypto.api.EncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CryptoPlugin extends AbstractPlugin {
    public static final String CRYPTO_PLUGIN_PREFIX = "org.seedstack.seed.crypto";
    public static final String MASTER_KEY_NAME = "master-key";
    public static final String DEFAULT_KEYSTORE_NAME = "keystore";
    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoPlugin.class);

    private final Map<String, EncryptionService> encryptionServices = new HashMap<String, EncryptionService>();

    @Override
    public String name() {
        return "seed-crypto-plugin";
    }

    @Override
    public Object nativeUnitModule() {
        return new CryptoModule(this.encryptionServices);
    }

    @Override
    public InitState init(InitContext initContext) {
        Plugin applicationPlugin = initContext.pluginsRequired().iterator().next();
        if (applicationPlugin instanceof ApplicationPlugin) {
            Configuration cryptoConfiguration = ((ApplicationPlugin) applicationPlugin).getApplication().getConfiguration().subset(CRYPTO_PLUGIN_PREFIX);
            EncryptionServiceFactory encryptionServiceFactory = new EncryptionServiceFactory();

            // Load configured keys
            String[] keys = cryptoConfiguration.getStringArray("keys");
            int keyCount = 0;

            if (keys != null) {
                keyCount = keys.length;
                JCADefinitionFactory jcaDefinitionFactory = new JCADefinitionFactory();
                KeyStoreDefinition defaultKS = jcaDefinitionFactory.createKeyStoreDefinition(cryptoConfiguration, DEFAULT_KEYSTORE_NAME);
                for (String key : keys) {
                    LOGGER.trace("Registering cryptographic key {}", key);
                    Configuration keyConfiguration = configurationForKey(cryptoConfiguration, key);
                    KeyDefinition keyDefinition = jcaDefinitionFactory.createKeyDefinition(keyConfiguration, key, defaultKS);
                    encryptionServices.put(key, encryptionServiceFactory.createEncryptionService(keyDefinition));
                }
            }

            // Load the master key
            LOGGER.trace("Registering master cryptographic key");
            encryptionServices.put("master", encryptionServiceFactory.createEncryptionService());
            LOGGER.debug("Registered {} cryptographic key(s)", keyCount + 1);
        }

        return InitState.INITIALIZED;
    }

    private Configuration configurationForKey(Configuration configuration, String keyName) {
        Configuration keyConfiguration = configuration.subset("key." + keyName);
        if (keyConfiguration.isEmpty()) {
            throw new RuntimeException("Key configuration [" + keyName + "] is not defined !");
        }

        return keyConfiguration;
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        return plugins;
    }
}
