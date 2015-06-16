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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.text.StrLookup;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.core.internal.application.ConfigurationLookup;
import org.seedstack.seed.core.internal.application.ConfigurationLookupRegistry;
import org.seedstack.seed.crypto.api.EncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoPlugin extends AbstractPlugin implements ConfigurationLookup {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoPlugin.class);

    private Map<String, EncryptionService> rsaServices = new HashMap<String, EncryptionService>();;

    private KeyStoreDefinition keyStoreDefinition;

    private static final String INTERNAL_CRYPTING = "master";

    private EncryptionServiceFactory asymetricCryptingFactory = new EncryptionServiceFactory();
    private CertificateDefinitionFactory certificateDefinitionFactory = new CertificateDefinitionFactory();

    @Override
    public String name() {
        return "seed-crypto-plugin";
    }

    @Override
    public Object nativeUnitModule() {
        return new CryptoModule(this.rsaServices);
    }

    @Override
    public InitState init(InitContext initContext) {
        for (Plugin plugin : initContext.pluginsRequired()) {
            if (plugin instanceof ApplicationPlugin) {
                Configuration configuration = ((ApplicationPlugin) plugin).getApplication().getConfiguration()
                        .subset("org.seedstack.seed.cryptography");

                // Looking for Keystore Informations
                keyStoreDefinition = new KeyStoreDefinition();
                keyStoreDefinition.setPassword(configuration.getString("keystore.password"));
                keyStoreDefinition.setPath(configuration.getString("keystore.path"));

                // Looking for all certificates configuration
                loadCertificates(configuration);
            }
        }

        return InitState.INITIALIZED;
    }

    /**
     * Load and check every certificates.
     * 
     * @param configuration properties to define {@link EncryptionService}
     */
    private void loadCertificates(Configuration configuration) {

        String certificatesConf = configuration.getString("keys");

        for (String conf : certificatesConf.split(";")) {
            LOGGER.debug("Looking configuration for key {}", conf);
            loadConfiguration(configuration, conf);
        }
    }

    /**
     * Load and check a certificate and a keystore.
     * 
     * @param configuration properties to define {@link EncryptionService}
     * @param keyName configuration name to load
     */
    private void loadConfiguration(Configuration configuration, String keyName) {
        Configuration customConfiguration = configuration.subset("key." + keyName);
        if (customConfiguration.isEmpty()) {
            throw new RuntimeException("Key configuration [" + keyName + "] is not defined !");
        }
        CertificateDefinition definition = certificateDefinitionFactory.getInstance(customConfiguration);

        // Check for a customized keystore for this definition
        KeyStoreDefinition customKeystoreDefinition = this.keyStoreDefinition;
        String customKeystore = customConfiguration.getString("keystore.path");
        if (customKeystore != null) {
            customKeystoreDefinition = new KeyStoreDefinition();
            customKeystoreDefinition.setPassword(customConfiguration.getString("keystore.password"));
            customKeystoreDefinition.setPath(customKeystore);
        }
        rsaServices.put(keyName, asymetricCryptingFactory.getInstance(customKeystoreDefinition, definition));

    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        return plugins;
    }

    /**
     * Default constructor to register to {@link ConfigurationLookupRegistry}. Used to add a {@link StrLookup} for password crypting in all
     * configurations files.
     */
    public CryptoPlugin() {
        ConfigurationLookupRegistry.getInstance().register("password", this);
    }

    @Override
    public StrLookup getLookup(Configuration configuration) {
        // Looking for internal seed crypto to create a StrLookup
        loadConfiguration(configuration.subset("org.seedstack.seed.cryptography"), INTERNAL_CRYPTING);
        return new PasswordLookup(rsaServices.get(INTERNAL_CRYPTING));
    }

}
