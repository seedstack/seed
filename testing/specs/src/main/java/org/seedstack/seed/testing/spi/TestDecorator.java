/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.spi;

/**
 * Interface for implementing test decorators that can execute code before and after each test.
 */
public interface TestDecorator {
    /**
     * Executed before each test.
     *
     * @param testContext the test context.
     */
    default void beforeTest(TestContext testContext) {
        // nothing to do
    }

    /**
     * Executed after each test.
     *
     * @param testContext the test context.
     */
    default void afterTest(TestContext testContext) {
        // nothing to do
    }
}
