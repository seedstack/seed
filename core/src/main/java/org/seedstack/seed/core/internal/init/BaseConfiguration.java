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
import org.seedstack.coffig.provider.SystemPropertyProvider;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.core.internal.configuration.PrioritizedProvider;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class BaseConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseConfiguration.class);
    private static final String BASE_CONFIGURATION_PROVIDER = "base";
    private static final String BASE_OVERRIDE_CONFIGURATION_PROVIDER = "base-override";
    private static final String ENV_CONFIGURATION_PROVIDER = "env";
    private static final String SYS_CONFIGURATION_PROVIDER = "sys";
    private static final int CONFIGURATION_BASE_PRIORITY = 0;
    private static final int CONFIGURATION_OVERRIDE_PRIORITY = 1000;
    private static final int CONFIGURATION_ENVIRONMENT_PRIORITY = 2000;
    private static final int CONFIGURATION_SYS_PRIORITY = 3000;
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
                        .registerProvider(BASE_CONFIGURATION_PROVIDER, buildJacksonProvider("application.yaml"), CONFIGURATION_BASE_PRIORITY)
                        .registerProvider(BASE_OVERRIDE_CONFIGURATION_PROVIDER, buildJacksonProvider("application.override.yaml"), CONFIGURATION_OVERRIDE_PRIORITY)
                        .registerProvider(ENV_CONFIGURATION_PROVIDER, new EnvironmentVariableProvider(), CONFIGURATION_ENVIRONMENT_PRIORITY)
                        .registerProvider(SYS_CONFIGURATION_PROVIDER, new SystemPropertyProvider(), CONFIGURATION_SYS_PRIORITY))
                .enableValidation(validatorFactory)
                .build();
    }

    private JacksonProvider buildJacksonProvider(String resourceName) {
        JacksonProvider jacksonProvider = new JacksonProvider();
        try {
            Enumeration<URL> configResources = SeedReflectionUtils.findMostCompleteClassLoader(Seed.class).getResources(resourceName);
            while (configResources.hasMoreElements()) {
                jacksonProvider.addSource(configResources.nextElement());
            }
        } catch (IOException e) {
            LOGGER.warn("I/O error during detection of configuration files", e);
        }
        return jacksonProvider;
    }
}
