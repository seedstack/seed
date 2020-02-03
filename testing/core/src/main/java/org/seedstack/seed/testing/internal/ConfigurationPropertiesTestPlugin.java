/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.internal;

import java.util.HashMap;
import java.util.Map;
import org.seedstack.seed.testing.ConfigurationProperty;
import org.seedstack.seed.testing.spi.TestContext;
import org.seedstack.seed.testing.spi.TestPlugin;
import org.seedstack.shed.reflect.Annotations;

public class ConfigurationPropertiesTestPlugin implements TestPlugin {
    @Override
    public boolean enabled(TestContext testContext) {
        return true;
    }

    @Override
    public Map<String, String> configurationProperties(TestContext testContext) {
        Map<String, String> configuration = new HashMap<>();

        // Configuration properties on the class
        Annotations.on(testContext.testClass())
                .includingMetaAnnotations()
                .findAll(ConfigurationProperty.class)
                .forEach(configProperty -> configuration.put(buildPropertyName(configProperty),
                        configProperty.value()));

        // Configuration properties on the method (completing/overriding class configuration properties)
        testContext.testMethod().ifPresent(testMethod -> Annotations.on(testMethod)
                .includingMetaAnnotations()
                .findAll(ConfigurationProperty.class)
                .forEach(configProperty -> configuration.put(buildPropertyName(configProperty),
                        configProperty.value())));

        return configuration;
    }

    private String buildPropertyName(ConfigurationProperty configProperty) {
        String[] profiles = configProperty.profiles();
        String name;
        if (profiles.length > 0) {
            name = String.format("%s<%s>", configProperty.name(), String.join(",", profiles));
        } else {
            name = configProperty.name();
        }
        return name;
    }
}
