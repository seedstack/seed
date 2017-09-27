/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.it.spi;

import java.util.List;
import java.util.Map;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

/**
 * A simple plugin for the {@link org.seedstack.seed.it.SeedITRunner} to add a Rules to the test.
 */
public interface ITRunnerPlugin {
    /**
     * The plugin can provide the class rules to apply to the test class. The provided
     * rules will be given to the kernel injector so the rules can use Inject
     * annotation.
     *
     * @param testClass the test class definition
     * @return A list of class rules to be applied to the test. An empty list if no
     * rule is to be applied.
     */
    List<Class<? extends TestRule>> provideClassRulesToApply(TestClass testClass);

    /**
     * The plugin can provide the rules to apply to the test object. The provided
     * rules will be given to the kernel injector so the rules can use Inject
     * annotation.
     *
     * @param testClass the test class definition
     * @param target    the test object
     * @return A list of rules to be applied to the test. An empty list if no
     * rule is to be applied.
     */
    List<Class<? extends TestRule>> provideTestRulesToApply(TestClass testClass, Object target);

    /**
     * The plugin can provide the rules to apply to each test method. The provided
     * rules will be given to the kernel injector so the rules can use Inject
     * annotation.
     *
     * @param testClass the test class definition
     * @param target    the test object
     * @return A list of rules to be applied to each test method. An empty list if no
     * rule is to be applied.
     */
    List<Class<? extends MethodRule>> provideMethodRulesToApply(TestClass testClass, Object target);

    /**
     * The plugin can provide some configuration for the started kernel. Only strings and arrays of string are
     * supported.
     * Arrays must be specified as comma-separated values.
     *
     * @param testClass the test class definition
     * @param method    the test method if the kernel is created per test, null otherwise.
     * @return the configuration map
     */
    Map<String, String> provideConfiguration(TestClass testClass, FrameworkMethod method);

    /**
     * The plugin can choose a kernel mode for the test. If multiple plugins require incompatible modes, an exception
     * will be thrown by the SEED IT runner.
     *
     * @param testClass the test class definition
     * @return the requested kernel mode
     */
    ITKernelMode kernelMode(TestClass testClass);
}
