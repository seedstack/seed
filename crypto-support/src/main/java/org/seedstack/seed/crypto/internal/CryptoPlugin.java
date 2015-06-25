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
    public static final String CRYPTO_PLUGIN_PREFIX = "org.seedstack.seed.cryptography";
    public static final String MASTER_KEY_NAME = "master";
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
        for (Plugin plugin : initContext.pluginsRequired()) {
            if (plugin instanceof ApplicationPlugin) {
                Configuration cryptoConfiguration = ((ApplicationPlugin) plugin).getApplication().getConfiguration().subset(CRYPTO_PLUGIN_PREFIX);
                EncryptionServiceFactory encryptionServiceFactory = new EncryptionServiceFactory();

                // Load configured keys
                String[] keys = cryptoConfiguration.getStringArray("keys");
                int keyCount = 0;

                if (keys != null) {
                    keyCount = keys.length;
                    for (String key : keys) {
                        LOGGER.trace("Registering cryptographic key {}", key);
                        encryptionServices.put(
                                key,
                                encryptionServiceFactory.createEncryptionService(
                                        encryptionServiceFactory.createKeyStoreDefinition(cryptoConfiguration, key),
                                        encryptionServiceFactory.createCertificateDefinition(cryptoConfiguration, key)
                                )
                        );
                    }
                }

                // Load the master key
                LOGGER.trace("Registering master cryptographic key");
                encryptionServices.put(MASTER_KEY_NAME,
                        encryptionServiceFactory.createEncryptionService(
                                encryptionServiceFactory.createKeyStoreDefinition(cryptoConfiguration, MASTER_KEY_NAME),
                                encryptionServiceFactory.createCertificateDefinition(cryptoConfiguration, MASTER_KEY_NAME)
                        )
                );

                LOGGER.debug("Registered {} cryptographic key(s)", keyCount + 1);
            }
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        return plugins;
    }
}
