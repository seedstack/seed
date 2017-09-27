/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.cli.internal;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.seedstack.seed.cli.WithCommandLine;
import org.seedstack.seed.it.spi.ITKernelMode;
import org.seedstack.seed.it.spi.ITRunnerPlugin;

/**
 * This plugin enables to run SEED command line applications from integration tests. It disables the global SEED
 * kernel start to start its own kernel for each test method.
 */
public class CommandLineITPlugin implements ITRunnerPlugin {
    @Override
    public List<Class<? extends TestRule>> provideClassRulesToApply(TestClass testClass) {
        return null;
    }

    @Override
    public List<Class<? extends TestRule>> provideTestRulesToApply(TestClass testClass, Object target) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Class<? extends MethodRule>> provideMethodRulesToApply(TestClass testClass, Object target) {
        if (checkForActivation(testClass)) {
            return Lists.newArrayList(CommandLineITRule.class);
        } else {
            return null;
        }
    }

    @Override
    public Map<String, String> provideConfiguration(TestClass testClass, FrameworkMethod frameworkMethod) {
        return null;
    }

    @Override
    public ITKernelMode kernelMode(TestClass testClass) {
        if (checkForActivation(testClass)) {
            return ITKernelMode.NONE;
        } else {
            return ITKernelMode.ANY;
        }
    }

    private boolean checkForActivation(TestClass testClass) {
        return !testClass.getAnnotatedMethods(
                WithCommandLine.class).isEmpty() || testClass.getJavaClass().getAnnotation(
                WithCommandLine.class) != null;
    }
}
