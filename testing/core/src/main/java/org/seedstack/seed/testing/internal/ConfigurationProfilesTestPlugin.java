/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.testing.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.seedstack.seed.testing.ConfigurationProfiles;
import org.seedstack.seed.testing.spi.TestContext;
import org.seedstack.seed.testing.spi.TestPlugin;

public class ConfigurationProfilesTestPlugin implements TestPlugin {
    private static final String DUMMY_VALUE = "__DUMMY__";
    private static final String SEEDSTACK_PROFILES = "seedstack.profiles";
    private static String previousValue;

    @Override
    public boolean enabled(TestContext testContext) {
        return true;
    }

    @Override
    public void beforeLaunch(TestContext testContext) {
        overrideProfiles(testContext);
    }

    @Override
    public void afterShutdown(TestContext testContext) {
        restoreProfiles();
    }

    private static synchronized void overrideProfiles(TestContext testContext) {
        Optional<String> profiles = gatherConfigurationProfiles(testContext);
        if (profiles.isPresent()) {
            if (previousValue != null) {
                throw new IllegalStateException("Attempt to override configuration profiles concurrently");
            } else {
                previousValue = System.getProperty(SEEDSTACK_PROFILES, DUMMY_VALUE);
                System.setProperty(SEEDSTACK_PROFILES, profiles.get());
            }
        }
    }

    private static synchronized void restoreProfiles() {
        if (previousValue != null) {
            try {
                if (DUMMY_VALUE.equals(previousValue)) {
                    System.clearProperty(SEEDSTACK_PROFILES);
                } else {
                    System.setProperty(SEEDSTACK_PROFILES, previousValue);
                }
            } finally {
                previousValue = null;
            }
        }
    }

    private static Optional<String> gatherConfigurationProfiles(TestContext testContext) {
        List<String> allProfiles = new ArrayList<>();
        AtomicBoolean annotationPresent = new AtomicBoolean(false);

        // Configuration profiles on the class
        ConfigurationProfiles classProfiles = testContext.testClass().getAnnotation(ConfigurationProfiles.class);
        if (classProfiles != null) {
            annotationPresent.set(true);
            allProfiles.addAll(Arrays.asList(classProfiles.value()));
        }

        // Configuration profiles on the method (taking precedence)
        testContext.testMethod().ifPresent(testMethod -> {
            ConfigurationProfiles methodProfiles = testMethod.getAnnotation(ConfigurationProfiles.class);
            if (methodProfiles != null) {
                annotationPresent.set(true);
                if (!methodProfiles.append()) {
                    allProfiles.clear();
                }
                allProfiles.addAll(Arrays.asList(methodProfiles.value()));
            }
        });

        return annotationPresent.get() ? Optional.of(String.join(",", allProfiles)) : Optional.empty();
    }
}
