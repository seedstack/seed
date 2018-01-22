/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.init;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.validation.ValidatorFactory;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.provider.EnvironmentProvider;
import org.seedstack.coffig.provider.JacksonProvider;
import org.seedstack.coffig.provider.PrefixProvider;
import org.seedstack.coffig.provider.PropertiesProvider;
import org.seedstack.coffig.provider.SystemPropertiesProvider;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.core.internal.configuration.PrioritizedProvider;
import org.seedstack.seed.spi.ConfigurationPriority;
import org.seedstack.shed.ClassLoaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseConfiguration.class);
    private static final String ENVIRONMENT_VARIABLES_PREFIX = "env";
    private static final String SYSTEM_PROPERTIES_PREFIX = "sys";
    private final Coffig baseConfiguration;

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
                                buildJacksonProvider("application.override.yaml", "application.override.yml",
                                        "application.override.json"),
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

    public static Coffig get() {
        return Holder.INSTANCE.baseConfiguration;
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

    private List<URL> getResources(String... resourceNames) {
        List<URL> result = new ArrayList<>();
        for (String resourceName : resourceNames) {
            try {
                Enumeration<URL> configResources = ClassLoaders.findMostCompleteClassLoader(Seed.class).getResources(
                        resourceName);
                while (configResources.hasMoreElements()) {
                    URL url = configResources.nextElement();
                    LOGGER.debug("Detected configuration resource: {}", url.toExternalForm());
                    result.add(url);
                }
            } catch (IOException e) {
                LOGGER.warn("I/O error during detection of configuration file " + resourceName, e);
            }
        }
        return result;
    }

    private static class Holder {
        private static final BaseConfiguration INSTANCE = new BaseConfiguration(GlobalValidatorFactory.get());
    }
}
