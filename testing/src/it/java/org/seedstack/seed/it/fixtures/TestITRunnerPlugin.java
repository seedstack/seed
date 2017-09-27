/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.it.fixtures;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.seedstack.seed.it.spi.ITKernelMode;
import org.seedstack.seed.it.spi.ITRunnerPlugin;

public class TestITRunnerPlugin implements ITRunnerPlugin {
    @Override
    public List<Class<? extends TestRule>> provideClassRulesToApply(TestClass testClass) {
        return null;
    }

    @Override
    public List<Class<? extends TestRule>> provideTestRulesToApply(TestClass testClass, Object target) {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Class<? extends MethodRule>> provideMethodRulesToApply(TestClass testClass, Object target) {
        return checkForActivation(testClass) ? Lists.newArrayList(TestKernelRule.class) : null;
    }

    @Override
    public Map<String, String> provideConfiguration(TestClass testClass, FrameworkMethod frameworkMethod) {
        Map<String, String> defaultConfiguration = new HashMap<>();

        defaultConfiguration.put("testKey", "testValue");

        if (frameworkMethod != null) {
            WithTestAnnotation annotation = frameworkMethod.getAnnotation(WithTestAnnotation.class);
            if (annotation != null) {
                defaultConfiguration.put(annotation.key(), annotation.value());
            }
        }

        return defaultConfiguration;
    }

    @Override
    public ITKernelMode kernelMode(TestClass testClass) {
        return checkForActivation(testClass) ? ITKernelMode.NONE : ITKernelMode.ANY;
    }

    private boolean checkForActivation(TestClass testClass) {
        return !testClass.getAnnotatedMethods(
                WithTestAnnotation.class).isEmpty() || testClass.getJavaClass().getAnnotation(
                WithTestAnnotation.class) != null;
    }
}
