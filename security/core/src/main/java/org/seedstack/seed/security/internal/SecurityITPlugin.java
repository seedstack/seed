/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import com.google.common.collect.Lists;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.seedstack.seed.it.spi.ITKernelMode;
import org.seedstack.seed.it.spi.ITRunnerPlugin;
import org.seedstack.seed.security.api.WithUser;

import java.util.List;
import java.util.Map;

/**
 * IT plugin for security. Handles the WithSecurity annotation and adds the rule to connect a user.
 * 
 * @author yves.dautremay@mpsa.com
 */
public class SecurityITPlugin implements ITRunnerPlugin {
    @Override
    public List<Class<? extends TestRule>> provideClassRulesToApply(TestClass testClass) {
        return null;
    }

    @Override
    public List<Class<? extends TestRule>> provideTestRulesToApply(TestClass testClass, Object target) {
        if (!testClass.getAnnotatedMethods(WithUser.class).isEmpty() || testClass.getJavaClass().getAnnotation(WithUser.class) != null) {
            return Lists.<Class<? extends TestRule>> newArrayList(SecurityITRule.class);
        } else {
            return null;
        }
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
        return ITKernelMode.ANY;
    }
}
