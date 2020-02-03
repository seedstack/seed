/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.spi;

import io.nuun.kernel.api.Kernel;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This interface defines a method that can launch a SeedStack application.
 * Implementations must be declared as a {@link java.util.ServiceLoader} service in META-INF/services to be detected.
 */
public interface SeedLauncher {
    /**
     * Launches the SeedStack application.
     *
     * @param args arguments of the SeedStack application.
     * @throws Exception when something goes wrong.
     */
    default void launch(String[] args) throws Exception {
        launch(args, new HashMap<>());
    }

    /**
     * Launches the SeedStack application with custom kernel parameters.
     *
     * @param args             arguments of the SeedStack application.
     * @param kernelParameters the custom kernel parameters.
     * @throws Exception when something goes wrong.
     */
    void launch(String[] args, Map<String, String> kernelParameters) throws Exception;

    /**
     * Refreshes the SeedStack application.
     *
     * @throws Exception when something goes wrong.
     */
    default void refresh() throws Exception {
        throw new UnsupportedOperationException("Refresh is not supported by this launcher");
    }

    /**
     * Returns the currently running kernel created by this launcher.
     *
     * @return the optional containing the currently running kernel or an empty optional if none is currently running.
     */
    default Optional<Kernel> getKernel() {
        return Optional.empty();
    }

    /**
     * Shutdown the SeedStack application.
     *
     * @throws Exception when something goes wrong.
     */
    void shutdown() throws Exception;
}
