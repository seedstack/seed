/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.junit4;

import org.junit.runners.model.InitializationError;
import org.seedstack.seed.testing.junit4.internal.JUnit4Runner;

/**
 * This runner can be used to run JUnit tests with SeedStack integration. Tests launched with this runner will benefit
 * from SeedStack features (injection, AOP interception, test extensions, ...).
 */
public class SeedITRunner extends JUnit4Runner {
    /**
     * Creates the runner for the corresponding test class.
     *
     * @param someClass the test class.
     * @throws InitializationError if an initialization error occurs.
     */
    public SeedITRunner(Class<?> someClass) throws InitializationError {
        super(someClass);
    }
}
