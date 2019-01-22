/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.arquillian.fixtures;

import com.google.inject.Injector;
import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.core.NuunCore;
import java.util.Set;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.seedstack.seed.core.Seed;

public class TestServletContainerInitializer implements ServletContainerInitializer, ServletContextListener {
    private static final String KERNEL_ATTRIBUTE_NAME = Kernel.class.getName();
    private static final String INJECTOR_ATTRIBUTE_NAME = Injector.class.getName();
    private Kernel kernel;

    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext servletContext) {
        kernel = Seed.createKernel(servletContext, NuunCore.newKernelConfiguration(), true);
        servletContext.setAttribute(KERNEL_ATTRIBUTE_NAME, kernel);
        servletContext.setAttribute(INJECTOR_ATTRIBUTE_NAME, kernel.objectGraph().as(Injector.class));
        servletContext.addListener(this);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // nothing to do
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        servletContext.removeAttribute(INJECTOR_ATTRIBUTE_NAME);
        servletContext.removeAttribute(KERNEL_ATTRIBUTE_NAME);
        if (kernel != null) {
            try {
                Seed.disposeKernel(kernel);
            } finally {
                kernel = null;
            }
        }
    }
}
