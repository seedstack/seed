/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.junit4.internal;

import io.nuun.kernel.api.Kernel;
import java.lang.reflect.Method;
import java.util.Optional;
import org.seedstack.seed.testing.spi.TestContext;

class JUnit4TestContext implements TestContext {
    private final Class<?> testClass;
    private Method testMethod;
    private Kernel kernel;

    JUnit4TestContext(Class<?> testClass) {
        this.testClass = testClass;
    }

    void setTestMethod(Method testMethod) {
        this.testMethod = testMethod;
    }

    void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    @Override
    public String testName() {
        return testClass.getSimpleName() + (testMethod == null ? "" : "." + testMethod.getName());
    }

    @Override
    public Class<?> testClass() {
        return testClass;
    }

    @Override
    public Optional<Method> testMethod() {
        return Optional.ofNullable(testMethod);
    }

    @Override
    public Optional<Kernel> testKernel() {
        return Optional.ofNullable(kernel);
    }

    @Override
    public String toString() {
        return testName();
    }
}
