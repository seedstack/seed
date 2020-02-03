/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.spi;

import io.nuun.kernel.api.Kernel;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * The testing context, allowing to access current test information.
 */
public interface TestContext {
    /**
     * Returns a name for the current test.
     *
     * @return the current test name.
     */
    String testName();

    /**
     * Returns the current test class.
     *
     * @return the current test class.
     */
    Class<?> testClass();

    /**
     * Returns the current test method if any.
     *
     * @return the current test method if any.
     */
    Optional<Method> testMethod();

    /**
     * Returns the kernel used for running the current test if any.
     *
     * @return the kernel used for the current test if any.
     */
    Optional<Kernel> testKernel();
}
