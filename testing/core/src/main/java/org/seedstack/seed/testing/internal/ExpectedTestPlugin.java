/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.internal;

import java.lang.reflect.Method;
import java.util.Optional;
import org.seedstack.seed.testing.Expected;
import org.seedstack.seed.testing.spi.TestContext;
import org.seedstack.seed.testing.spi.TestPlugin;

public class ExpectedTestPlugin implements TestPlugin {
    @Override
    public boolean enabled(TestContext testContext) {
        return true;
    }

    @Override
    public Optional<Class<? extends Exception>> expectedException(TestContext testContext) {
        // Expected exception on the method (taking precedence)
        Optional<Method> testMethod = testContext.testMethod();
        if (testMethod.isPresent()) {
            Expected expected = testMethod.get().getAnnotation(Expected.class);
            if (expected != null && !Expected.None.class.equals(expected.value())) {
                return Optional.of(expected.value());
            }
        }

        // Expected exception on the class
        Expected classExpected = testContext.testClass().getAnnotation(Expected.class);
        if (classExpected != null && !Expected.None.class.equals(classExpected.value())) {
            return Optional.of(classExpected.value());
        }

        return Optional.empty();
    }
}
