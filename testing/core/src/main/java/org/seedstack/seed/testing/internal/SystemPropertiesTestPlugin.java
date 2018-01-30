/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.testing.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.seedstack.seed.testing.SystemProperty;
import org.seedstack.seed.testing.spi.TestContext;
import org.seedstack.seed.testing.spi.TestPlugin;
import org.seedstack.shed.reflect.Annotations;

public class SystemPropertiesTestPlugin implements TestPlugin {
    private static Map<String, String> previousProperties;

    @Override
    public boolean enabled(TestContext testContext) {
        return true;
    }

    @Override
    public void beforeLaunch(TestContext testContext) {
        overrideProperties(testContext);
    }

    @Override
    public void afterShutdown(TestContext testContext) {
        restoreProperties();
    }

    private static synchronized void overrideProperties(TestContext testContext) {
        Properties overridingProperties = gatherSystemProperties(testContext);
        if (!overridingProperties.isEmpty()) {
            if (previousProperties != null) {
                throw new IllegalStateException("Attempt to override system properties concurrently");
            } else {
                previousProperties = new HashMap<>();
                for (String propertyName : overridingProperties.stringPropertyNames()) {
                    previousProperties.put(propertyName, System.getProperty(propertyName));
                    System.setProperty(propertyName, overridingProperties.getProperty(propertyName));
                }
            }
        }
    }

    private static synchronized void restoreProperties() {
        if (previousProperties != null) {
            try {
                for (Map.Entry<String, String> property : previousProperties.entrySet()) {
                    String value = property.getValue();
                    if (value == null) {
                        System.clearProperty(property.getKey());
                    } else {
                        System.setProperty(property.getKey(), value);
                    }
                }
            } finally {
                previousProperties = null;
            }
        }
    }

    private static Properties gatherSystemProperties(TestContext testContext) {
        Properties systemProperties = new Properties();

        // System properties on the class
        Annotations.on(testContext.testClass())
                .includingMetaAnnotations()
                .findAll(SystemProperty.class)
                .forEach(systemProperty -> systemProperties.put(systemProperty.name(),
                        systemProperty.value()));

        // System properties on the method (completing/overriding class system properties)
        testContext.testMethod().ifPresent(testMethod -> Annotations.on(testMethod)
                .includingMetaAnnotations()
                .findAll(SystemProperty.class)
                .forEach(systemProperty -> systemProperties.put(systemProperty.name(),
                        systemProperty.value())));

        return systemProperties;
    }
}
