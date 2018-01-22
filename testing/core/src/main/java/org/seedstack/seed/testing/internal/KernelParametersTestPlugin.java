/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.internal;

import java.util.HashMap;
import java.util.Map;
import org.seedstack.seed.testing.KernelParameter;
import org.seedstack.seed.testing.spi.TestPlugin;
import org.seedstack.seed.testing.spi.TestContext;

public class KernelParametersTestPlugin implements TestPlugin {
    private static final String TEST_CLASS_KERNEL_PARAMETER = "seedstack.it.testClassName";

    @Override
    public boolean enabled(TestContext testContext) {
        return true;
    }

    @Override
    public Map<String, String> kernelParameters(TestContext testContext) {
        Map<String, String> kernelParameters = new HashMap<>();

        // Kernel parameters on the class
        for (KernelParameter kernelParameter : testContext.testClass().getAnnotationsByType(KernelParameter.class)) {
            kernelParameters.put(kernelParameter.name(), kernelParameter.value());
        }

        // Kernel parameters on the method (completing/overriding class parameters)
        testContext.testMethod().ifPresent(testMethod -> {
            for (KernelParameter kernelParameter : testMethod.getAnnotationsByType(KernelParameter.class)) {
                kernelParameters.put(kernelParameter.name(), kernelParameter.value());
            }
        });

        // The test full class name
        kernelParameters.put(TEST_CLASS_KERNEL_PARAMETER, testContext.testClass().getName());

        return kernelParameters;
    }
}
