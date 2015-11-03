/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it.internal;

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.seedstack.seed.it.KernelMode;
import org.seedstack.seed.it.spi.ITKernelMode;
import org.seedstack.seed.it.spi.ITRunnerPlugin;

import java.util.List;
import java.util.Map;

/**
 * This IT plugin allow to change the test class kernel mode explicitly.
 *
 * @author adrien.lauer@mpsa.com
 */
public class KernelModeITPlugin implements ITRunnerPlugin {
    @Override
    public List<Class<? extends TestRule>> provideClassRulesToApply(TestClass testClass) {
        return null;
    }

    @Override
    public List<Class<? extends TestRule>> provideTestRulesToApply(TestClass testClass, Object target) {
        return null;
    }

    @Override
    public List<Class<? extends MethodRule>> provideMethodRulesToApply(TestClass testClass, Object target) {
        return null;
    }

    @Override
    public Map<String, String> provideDefaultConfiguration(TestClass testClass, FrameworkMethod frameworkMethod) {
        return null;
    }

    @Override
    public ITKernelMode kernelMode(TestClass testClass) {
        KernelMode kernelMode = testClass.getJavaClass().getAnnotation(KernelMode.class);
        if (kernelMode != null) {
            return kernelMode.value();
        } else {
            return ITKernelMode.ANY;
        }
    }
}
