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
import org.seedstack.shed.exception.SeedException;
import org.seedstack.seed.core.Seed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import java.util.Enumeration;
import java.util.Set;

public class SeedServletContainerInitializer implements ServletContainerInitializer, ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeedServletContainerInitializer.class);
    private Kernel kernel;

    @Override
    public void onStartup(Set<Class<?>> servletContextConfigurerClasses, ServletContext servletContext) throws ServletException {
        try {
            kernel = Seed.createKernel(servletContext, buildKernelConfiguration(servletContext), true);
            servletContext.setAttribute(ServletContextUtils.KERNEL_ATTRIBUTE_NAME, kernel);
            servletContext.setAttribute(ServletContextUtils.INJECTOR_ATTRIBUTE_NAME, kernel.objectGraph().as(Injector.class));
        } catch (SeedException e) {
            handleException(e);
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw SeedException.wrap(e, WebErrorCode.UNEXPECTED_EXCEPTION);
        }

        servletContext.addListener(this);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // nothing to do
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (kernel != null) {
            try {
                ServletContext servletContext = sce.getServletContext();
                servletContext.removeAttribute(ServletContextUtils.INJECTOR_ATTRIBUTE_NAME);
                servletContext.removeAttribute(ServletContextUtils.KERNEL_ATTRIBUTE_NAME);
                Seed.disposeKernel(kernel);
            } catch (SeedException e) {
                handleException(e);
                throw e;
            } catch (Exception e) {
                handleException(e);
                throw SeedException.wrap(e, WebErrorCode.UNEXPECTED_EXCEPTION);
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

    private void handleException(Throwable t) {
        LOGGER.error("An exception occurred during web application startup, collecting diagnostic information");
        Seed.diagnostic(kernel).dumpDiagnosticReport(t);
    }
}
