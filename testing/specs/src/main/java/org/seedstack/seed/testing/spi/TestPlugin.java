/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.testing.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.seedstack.seed.spi.SeedLauncher;
import org.seedstack.seed.testing.LaunchMode;

/**
 * Interface for implementing test plugins that can alter the behavior of tests run with SeedStack.
 */
public interface TestPlugin {
    /**
     * This method should return true to enable the plugin or false to completely disable it.
     *
     * @param testContext the test context.
     * @return true if the plugin should be enabled, false otherwise.
     */
    boolean enabled(TestContext testContext);

    /**
     * Returns the list of {@link TestDecorator} to apply to each test.
     *
     * @return the list of {@link TestDecorator} classes.
     */
    default List<Class<? extends TestDecorator>> decorators() {
        return new ArrayList<>();
    }

    /**
     * This method is executed before launching the SeedStack environment used for testing.
     *
     * @param testContext the test context.
     */
    default void beforeLaunch(TestContext testContext) {
        // nothing to do
    }

    /**
     * This method is executed after shutting down the SeedStack environment used for testing.
     *
     * @param testContext the test context.
     */
    default void afterShutdown(TestContext testContext) {
        // nothing to do
    }

    /**
     * Allow the plugin to specify arguments used for launching the SeedStack environment used for testing.
     *
     * @param testContext the test context.
     * @return the arguments used for launching the test environment.
     */
    default String[] arguments(TestContext testContext) {
        return new String[0];
    }

    /**
     * Allow the plugin to specify configuration properties for the SeedStack environment used for testing.
     *
     * @param testContext the test context.
     * @return the configuration properties for the test environment.
     */
    default Map<String, String> configurationProperties(TestContext testContext) {
        return new HashMap<>();
    }

    /**
     * Allow the plugin to specify the exception that is expected to be the outcome of the SeedStack environment
     * launch.
     *
     * @param testContext the test context.
     * @return the exception that is expected to be the outcome of the test environment launch.
     */
    default Optional<Class<? extends Exception>> expectedException(TestContext testContext) {
        return Optional.empty();
    }

    /**
     * Allow the plugin to specify a particular {@link LaunchMode} for the test. The {@link LaunchMode#ANY} mode
     * specifies that this plugin doesn't require a particular mode.
     *
     * @param testContext the test context.
     * @return the launch mode to be used for the test environment.
     */
    default LaunchMode launchMode(TestContext testContext) {
        return LaunchMode.ANY;
    }

    /**
     * Allow the plugin to specify a particular {@link SeedLauncher} to be used to launch the SeedStack environment
     * used for testing.
     *
     * @param testContext the test context.
     * @return the {@link SeedLauncher} to be used to launch the test environment.
     */
    default Optional<? extends SeedLauncher> launcher(TestContext testContext) {
        return Optional.empty();
    }

    /**
     * Allow the plugin to specify if the launch should occur in a separate thread or the main thread.
     *
     * @param testContext the test context.
     * @return true if the launch should be done in a new thread, false if it should be done in the main thread.
     */
    default boolean separateThread(TestContext testContext) {
        return false;
    }

    /**
     * Allow the plugin to specify kernel parameters for the SeedStack environment used for testing.
     *
     * @param testContext the test context.
     * @return the kernel parameters for the test environment.
     */
    default Map<String, String> kernelParameters(TestContext testContext) {
        return new HashMap<>();
    }
}
