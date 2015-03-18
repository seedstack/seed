/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.seedstack.seed.cli.api.WithCommandLine;
import org.seedstack.seed.cli.rule.CommandLineITRule;
import org.seedstack.seed.it.spi.ITKernelMode;
import org.seedstack.seed.it.spi.ITRunnerPlugin;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runners.model.TestClass;

import java.util.List;
import java.util.Map;

/**
 * This plugin enables to run SEED command line applications from integration tests. It disables the global SEED
 * kernel start to start its own kernel for each test method.
 *
 * @author adrien.lauer@mpsa.com
 */
public class CommandLineITPlugin implements ITRunnerPlugin {
    @Override
    public List<Class<? extends TestRule>> provideClassRulesToApply(TestClass testClass) {
        return Lists.newArrayList();
    }

    @Override
    public List<Class<? extends TestRule>> provideTestRulesToApply(TestClass testClass, Object target) {
        return Lists.newArrayList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Class<? extends MethodRule>> provideMethodRulesToApply(TestClass testClass, Object target) {
        if (checkForActivation(testClass)) {
            return Lists.<Class<? extends MethodRule>>newArrayList(CommandLineITRule.class);
        } else {
            return Lists.newArrayList();
        }
    }

    @Override
    public Map<String, String> provideDefaultConfiguration(TestClass testClass) {
        return Maps.newHashMap();
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
        return !testClass.getAnnotatedMethods(WithCommandLine.class).isEmpty() || testClass.getJavaClass().getAnnotation(WithCommandLine.class) != null;
    }
}
