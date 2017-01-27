/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;

import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.provider.EnvironmentProvider;
import org.seedstack.coffig.provider.JacksonProvider;
import org.seedstack.coffig.provider.PrefixProvider;
import org.seedstack.coffig.provider.PropertiesProvider;
import org.seedstack.coffig.provider.SystemPropertiesProvider;
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
import java.util.HashSet;
import java.util.Set;

public class BaseConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseConfiguration.class);
    private static final String ENVIRONMENT_VARIABLES_PREFIX = "env";
    private static final String SYSTEM_PROPERTIES_PREFIX = "sys";
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
                                buildJacksonProvider("application.yaml", "application.yml", "application.json"),
                                ConfigurationPriority.BASE
                        )
                        .registerProvider(
                                buildPropertiesProvider("application.properties"),
                                ConfigurationPriority.BASE
                        )
                        .registerProvider(
                                buildJacksonProvider("application.override.yaml", "application.override.yml", "application.override.json"),
                                ConfigurationPriority.BASE_OVERRIDE
                        )
                        .registerProvider(
                                buildPropertiesProvider("application.override.properties"),
                                ConfigurationPriority.BASE_OVERRIDE
                        )
                        .registerProvider(
                                new PrefixProvider<>(ENVIRONMENT_VARIABLES_PREFIX, new EnvironmentProvider()),
                                ConfigurationPriority.ENVIRONMENT_VARIABLES
                        )
                        .registerProvider(
                                new PrefixProvider<>(SYSTEM_PROPERTIES_PREFIX, new SystemPropertiesProvider()),
                                ConfigurationPriority.SYSTEM_PROPERTIES
                        ))
                .enableValidation(validatorFactory)
                .build();
    }

    private JacksonProvider buildJacksonProvider(String... resourceNames) {
        JacksonProvider jacksonProvider = new JacksonProvider();
        getResources(resourceNames).forEach(jacksonProvider::addSource);
        return jacksonProvider;
    }

    private PropertiesProvider buildPropertiesProvider(String... resourceNames) {
        PropertiesProvider propertiesProvider = new PropertiesProvider();
        getResources(resourceNames).forEach(propertiesProvider::addSource);
        return propertiesProvider;
    }

    private Set<URL> getResources(String... resourceNames) {
        Set<URL> result = new HashSet<>();
        for (String resourceName : resourceNames) {
            try {
                Enumeration<URL> configResources = ClassLoaders.findMostCompleteClassLoader(Seed.class).getResources(resourceName);
                while (configResources.hasMoreElements()) {
                    result.add(configResources.nextElement());
                }
            } catch (IOException e) {
                LOGGER.warn("I/O error during detection of configuration file " + resourceName, e);
            }
        }
        return result;
    }
}
