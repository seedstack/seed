/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it.fixtures;

import com.google.inject.Injector;
import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.config.KernelConfiguration;
import io.nuun.kernel.core.NuunCore;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.seedstack.seed.it.api.ITBind;
import org.seedstack.seed.it.spi.KernelRule;

import javax.inject.Singleton;

@Singleton
@ITBind
public class TestKernelRule implements MethodRule, KernelRule {
    private KernelConfiguration kernelConfiguration;

    @Override
    public void acceptKernelConfiguration(KernelConfiguration kernelConfiguration) {
        this.kernelConfiguration = kernelConfiguration;
    }

    public KernelConfiguration getKernelConfiguration() {
        return kernelConfiguration;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                Kernel kernel = null;

                if (method.getAnnotation(WithTestAnnotation.class) != null) {
                    kernel = NuunCore.createKernel(kernelConfiguration);
                    kernel.init();
                    kernel.start();
                    kernel.objectGraph().as(Injector.class).injectMembers(target);
                }

                try {
                    base.evaluate();
                } finally {
                    if (kernel != null && kernel.isStarted()) {
                        kernel.stop();
                    }
                }
            }
        };
    }
}
