/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.seedstack.seed.it.api.KernelMode;
import org.seedstack.seed.it.spi.ITKernelMode;
import org.seedstack.seed.it.spi.ITRunnerPlugin;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runners.model.TestClass;

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
        return Lists.newArrayList();
    }

    @Override
    public List<Class<? extends TestRule>> provideTestRulesToApply(TestClass testClass, Object target) {
        return Lists.newArrayList();
    }

    @Override
    public List<Class<? extends MethodRule>> provideMethodRulesToApply(TestClass testClass, Object target) {
        return Lists.newArrayList();
    }

    @Override
    public Map<String, String> provideDefaultConfiguration(TestClass testClass) {
        return Maps.newHashMap();
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
