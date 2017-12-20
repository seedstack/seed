/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.spi;

/**
 * Interface to use to discover optional dependencies.
 */
public interface DependencyProvider {

    /**
     * Return the class to check in the classpath.
     *
     * @return the class to check in the classpath.
     */
    String getClassToCheck();
}
