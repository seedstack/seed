/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.internal;

import java.util.HashMap;
import java.util.Map;
import org.seedstack.seed.testing.KernelParameter;
import org.seedstack.seed.testing.spi.TestContext;
import org.seedstack.seed.testing.spi.TestPlugin;
import org.seedstack.shed.reflect.Annotations;

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
        Annotations.on(testContext.testClass())
                .includingMetaAnnotations()
                .findAll(KernelParameter.class)
                .forEach(kernelParameter -> kernelParameters.put(kernelParameter.name(),
                        kernelParameter.value()));

        // Kernel parameters on the method (completing/overriding class kernel parameters)
        testContext.testMethod().ifPresent(testMethod -> Annotations.on(testMethod)
                .includingMetaAnnotations()
                .findAll(KernelParameter.class)
                .forEach(kernelParameter -> kernelParameters.put(kernelParameter.name(),
                        kernelParameter.value())));

        // The test full class name
        kernelParameters.put(TEST_CLASS_KERNEL_PARAMETER, testContext.testClass().getName());

        return kernelParameters;
    }
}
