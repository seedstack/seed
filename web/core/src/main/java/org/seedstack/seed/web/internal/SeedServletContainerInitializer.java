/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal;

import static org.seedstack.seed.web.internal.ServletContextUtils.INJECTOR_ATTRIBUTE_NAME;
import static org.seedstack.seed.web.internal.ServletContextUtils.KERNEL_ATTRIBUTE_NAME;

import com.google.inject.Injector;
import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.config.KernelConfiguration;
import io.nuun.kernel.core.NuunCore;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Set;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionCookieConfig;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.web.WebConfig;
import org.seedstack.shed.exception.BaseException;

public class SeedServletContainerInitializer implements ServletContainerInitializer, ServletContextListener {
    private Kernel kernel;

    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext servletContext) {
        WebConfig webConfig = Seed.baseConfiguration().get(WebConfig.class);
        servletContext.setSessionTrackingModes(webConfig.sessions().getTrackingModes());
        copyConfig(webConfig.sessions().cookie(), servletContext.getSessionCookieConfig());

        try {
            kernel = Seed.createKernel(servletContext, buildKernelConfiguration(servletContext), true);
        } catch (Exception e) {
            handleException(e);
        }

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
            } catch (Exception e) {
                handleException(e);
            } finally {
                kernel = null;
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
        if (!Seed.hasLifecycleExceptionHandler()) {
            Seed.diagnostic().dumpDiagnosticReport(translated);
        }
        throw translated;
    }

    private void copyConfig(WebConfig.SessionsConfig.CookieConfig src, SessionCookieConfig dest) {
        Optional.ofNullable(src.getComment()).ifPresent(dest::setComment);
        Optional.ofNullable(src.getDomain()).ifPresent(dest::setDomain);
        Optional.ofNullable(src.getName()).ifPresent(dest::setName);
        Optional.ofNullable(src.getPath()).ifPresent(dest::setPath);
        dest.setHttpOnly(src.isHttpOnly());
        dest.setSecure(src.isSecure());
        dest.setMaxAge(src.getMaxAge());
    }
}
