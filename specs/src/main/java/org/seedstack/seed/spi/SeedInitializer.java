/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.spi;

import org.seedstack.coffig.Coffig;

/**
 * <p>This interface defines methods that are called at various stages of the SeedStack JVM initialization and shutdown
 * process. Implementations must be declared as a {@link java.util.ServiceLoader} service in META-INF/services to be
 * detected.</p>
 *
 * <p>Classes implementing this interface can be annotated with {@link javax.annotation.Priority} to specify an
 * absolute order among them.</p>
 *
 * <p>A single instance of each implementation is created and using throughout the whole lifecycle.</p>
 */
public interface SeedInitializer {
    /**
     * Called before SeedStack initialization.
     */
    void beforeInitialization();

    /**
     * Called during SeedStack initialization, just after base configuration has been made available.
     *
     * @param configuration the base configuration.
     */
    void onInitialization(Coffig configuration);

    /**
     * Called after SeedStack initialization has been completed.
     */
    void afterInitialization();

    /**
     * Called after SeedStack refresh has been completed.
     */
    void afterRefresh();

    /**
     * Called at explicit SeedStack global state cleanup.
     */
    void onClose();
}
