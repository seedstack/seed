/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.spi;

import io.nuun.kernel.api.Plugin;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * This interface can be implemented by a Nuun {@link Plugin} to declare itself as providing a SeedStack tool.
 * Implementations must be declared as a {@link java.util.ServiceLoader} service in META-INF/services to be detected.
 */
public interface SeedTool extends Callable<Integer>, Plugin {
    /**
     * Returns the unique name of the tool.
     *
     * @return the unique name of the tool.
     */
    String toolName();

    /**
     * Determines the start mode.
     * <ul>
     * <li>MINIMAL will disable plugin auto-detection and only enable plugins returned by the
     * {@link #pluginsToLoad()} method.</li>
     * <li>FULL will start the application normally with plugin auto-detection enabled. Plugins returned by the
     * {@link #pluginsToLoad()}
     * method will also be loaded.</li>
     * </ul>
     *
     * @return the start mode of the tool.
     */
    StartMode startMode();

    /**
     * Returns the plugins to load.
     *
     * @return a collection of plugins to explicitly load for this tool.
     */
    Collection<Class<?>> pluginsToLoad();

    /**
     * The start mode of a SeedStack tool.
     *
     * @see SeedTool#startMode()
     */
    enum StartMode {
        MINIMAL,
        FULL
    }
}
