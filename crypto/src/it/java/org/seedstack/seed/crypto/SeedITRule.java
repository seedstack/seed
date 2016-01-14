/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 11 juin 2015
 */
package org.seedstack.seed.crypto;

import static io.nuun.kernel.core.NuunCore.createKernel;
import static io.nuun.kernel.core.NuunCore.newKernelConfiguration;
import io.nuun.kernel.api.Kernel;

import java.lang.reflect.Method;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * {@link Rule} to start/stop the {@link Kernel} and to inject a {@link BeforeKernel} method.
 * 
 * @author thierry.bouvet@mpsa.com
 */
public class SeedITRule implements TestRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeedITRule.class);

    private Object target;

    public SeedITRule(Object target) {
        super();
        this.target = target;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {

        return new Statement() {

            private Kernel kernel;

            @Override
            public void evaluate() throws Throwable {
                for (Method method : target.getClass().getDeclaredMethods()) {
                    if (method.getAnnotation(BeforeKernel.class) != null) {
                        new FrameworkMethod(method).invokeExplosively(target);
                    }
                }

                LOGGER.info("Starting kernel");
                startKernel();

                try {
                    base.evaluate();
                } finally {
                    LOGGER.info("Stopping kernel");
                    stopKernel();
                }

            }

            private void startKernel() {
                kernel = createKernel(newKernelConfiguration());
                kernel.init();
                kernel.start();
                kernel.objectGraph().as(Injector.class).injectMembers(target);
            }

            private void stopKernel() {
                kernel.stop();
            }
        };
    }

}
