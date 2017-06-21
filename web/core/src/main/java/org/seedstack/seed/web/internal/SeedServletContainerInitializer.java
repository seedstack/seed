/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import com.google.inject.Injector;
import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.config.KernelConfiguration;
import io.nuun.kernel.core.NuunCore;
import org.seedstack.seed.core.Seed;
import org.seedstack.shed.exception.BaseException;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import java.util.Enumeration;
import java.util.Set;

public class SeedServletContainerInitializer implements ServletContainerInitializer, ServletContextListener {
    private Kernel kernel;

    @Override
    public void onStartup(Set<Class<?>> servletContextConfigurerClasses, ServletContext servletContext) throws ServletException {
        try {
            kernel = Seed.createKernel(servletContext, buildKernelConfiguration(servletContext), true);
            servletContext.setAttribute(ServletContextUtils.KERNEL_ATTRIBUTE_NAME, kernel);
            servletContext.setAttribute(ServletContextUtils.INJECTOR_ATTRIBUTE_NAME, kernel.objectGraph().as(Injector.class));
        } catch (Exception e) {
            handleException(e);
        }

        servletContext.addListener(this);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // nothing to do, already initialized
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (kernel != null) {
            try {
                ServletContext servletContext = sce.getServletContext();
                servletContext.removeAttribute(ServletContextUtils.INJECTOR_ATTRIBUTE_NAME);
                servletContext.removeAttribute(ServletContextUtils.KERNEL_ATTRIBUTE_NAME);
                Seed.disposeKernel(kernel);
            } catch (Exception e) {
                handleException(e);
            }
        }
    }

    private KernelConfiguration buildKernelConfiguration(ServletContext servletContext) {
        KernelConfiguration kernelConfiguration = NuunCore.newKernelConfiguration();

        Enumeration<?> initParameterNames = servletContext.getInitParameterNames();
        while (initParameterNames.hasMoreElements()) {
            String parameterName = (String) initParameterNames.nextElement();
            if (parameterName != null && !parameterName.isEmpty()) {
                String parameterValue = servletContext.getInitParameter(parameterName);
                kernelConfiguration.param(parameterName, parameterValue);
            }
        }

        return kernelConfiguration;
    }

    private void handleException(Exception e) throws BaseException {
        BaseException translated = Seed.translateException(e);
        Seed.diagnostic().dumpDiagnosticReport(translated);
        throw translated;
    }
}
