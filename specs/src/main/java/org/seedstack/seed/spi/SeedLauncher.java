/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.spi;

/**
 * This interface defines a method that can launch a Seed application.
 * Implementations must be declared as a {@link java.util.ServiceLoader} service in META-INF/services to be detected.
 */
public interface SeedLauncher {
    /**
     * The method that launches the Seed application.
     *
     * @param args arguments of the Seed application.
     * @throws Exception when something goes wrong.
     */
    void launch(String[] args) throws Exception;

    /**
     * This method is called when the application is requested to shutdown.
     *
     * @throws Exception when something goes wrong.
     */
    void shutdown() throws Exception;

    default void refresh() throws Exception {
        throw new UnsupportedOperationException("Refresh is not supported by this launcher");
    }
}
