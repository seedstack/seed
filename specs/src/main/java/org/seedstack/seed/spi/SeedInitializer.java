/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.spi;

import org.seedstack.coffig.Coffig;

/**
 * This interface defines two methods that are called at Seed JVM initialization and close.
 * Implementations must be declared as a {@link java.util.ServiceLoader} service in META-INF/services to be detected.
 */
public interface SeedInitializer {
    /**
     * Called at Seed JVM-wide initialization.
     *
     * @param configuration the base configuration.
     */
    void onInitialization(Coffig configuration);

    /**
     * Called at Seed JVM-wide close.
     */
    void onClose();
}
