/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.it.fixtures;

import com.google.inject.Injector;
import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.config.KernelConfiguration;
import javax.inject.Singleton;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.it.ITBind;
import org.seedstack.seed.it.spi.KernelRule;

@Singleton
@ITBind
public class TestKernelRule implements MethodRule, KernelRule {
    private KernelConfiguration kernelConfiguration;

    @Override
    public void acceptKernelConfiguration(KernelConfiguration kernelConfiguration) {
        this.kernelConfiguration = kernelConfiguration;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                Kernel kernel = null;

                if (method.getAnnotation(WithTestAnnotation.class) != null) {
                    kernel = Seed.createKernel(null, kernelConfiguration, true);
                    kernel.objectGraph().as(Injector.class).injectMembers(target);
                }

                try {
                    base.evaluate();
                } finally {
                    if (kernel != null) {
                        Seed.disposeKernel(kernel);
                    }
                }
            }
        };
    }
}
