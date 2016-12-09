/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;

import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.provider.EnvironmentVariableProvider;
import org.seedstack.coffig.provider.JacksonProvider;
import org.seedstack.coffig.provider.PrefixProvider;
import org.seedstack.coffig.provider.SystemPropertyProvider;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.core.internal.configuration.ConfigurationPriority;
import org.seedstack.seed.core.internal.configuration.PrioritizedProvider;
import org.seedstack.shed.ClassLoaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class BaseConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseConfiguration.class);
    private static final String ENVIRONMENT_VARIABLES_PREFIX = "env";
    private static final String SYSTEM_PROPERTIES_PREFIX = "sys";
    private static final String BASE_CONFIGURATION_PROVIDER = "base-config";
    private static final String BASE_OVERRIDE_CONFIGURATION_PROVIDER = "base-config-override";
    private static final String ENV_CONFIGURATION_PROVIDER = "environment-variables";
    private static final String SYS_CONFIGURATION_PROVIDER = "system-properties";
    private final Coffig baseConfiguration;

    private static class Holder {
        private static final BaseConfiguration INSTANCE = new BaseConfiguration(GlobalValidatorFactory.get());
    }

    public static Coffig get() {
        return Holder.INSTANCE.baseConfiguration;
    }

    private BaseConfiguration(ValidatorFactory validatorFactory) {
        baseConfiguration = Coffig.builder()
                .withProviders(new PrioritizedProvider()
                        .registerProvider(
                                BASE_CONFIGURATION_PROVIDER,
                                buildJacksonProvider("application.yaml", "application.yml", "application.json"),
                                ConfigurationPriority.BASE
                        )
                        .registerProvider(
                                BASE_OVERRIDE_CONFIGURATION_PROVIDER,
                                buildJacksonProvider("application.override.yaml", "application.override.yml", "application.override.json"),
                                ConfigurationPriority.BASE_OVERRIDE
                        )
                        .registerProvider(
                                ENV_CONFIGURATION_PROVIDER,
                                new PrefixProvider<>(ENVIRONMENT_VARIABLES_PREFIX, new EnvironmentVariableProvider()),
                                ConfigurationPriority.ENVIRONMENT_VARIABLES
                        )
                        .registerProvider(
                                SYS_CONFIGURATION_PROVIDER,
                                new PrefixProvider<>(SYSTEM_PROPERTIES_PREFIX, new SystemPropertyProvider()),
                                ConfigurationPriority.SYSTEM_PROPERTIES
                        ))
                .enableValidation(validatorFactory)
                .build();
    }

    private JacksonProvider buildJacksonProvider(String... resourceNames) {
        JacksonProvider jacksonProvider = new JacksonProvider();
        for (String resourceName : resourceNames) {
            try {
                Enumeration<URL> configResources = ClassLoaders.findMostCompleteClassLoader(Seed.class).getResources(resourceName);
                while (configResources.hasMoreElements()) {
                    jacksonProvider.addSource(configResources.nextElement());
                }
            } catch (IOException e) {
                LOGGER.warn("I/O error during detection of configuration files", e);
            }
        }
        return jacksonProvider;
    }
}
