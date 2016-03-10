/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.listener;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import io.nuun.kernel.api.Kernel;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.web.internal.WebErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * This context listener has the responsibility to initialize the Seed framework in a Web environment. This listener
 * should only be declared directly in a web.xml file when under a Servlet 2.5 level container. Servlet 3+ capable containers
 * will automatically initialize Seed.
 *
 * @author adrien.lauer@mpsa.com
 */
public class SeedServletContextListener extends GuiceServletContextListener {
    public static final String KERNEL_ATTRIBUTE_NAME = Kernel.class.getName();
    private static final Logger LOGGER = LoggerFactory.getLogger(SeedServletContextListener.class);

    private Kernel kernel;

    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        if (servletContext.getAttribute(KERNEL_ATTRIBUTE_NAME) != null) {
            throw new RuntimeException(
                    SeedException.createNew(WebErrorCode.SEED_ALREADY_INITIALIZED)
                            .put("servlet-context-name", String.valueOf(servletContext.getServletContextName()))
                            .toString()
            );
        }

        try {
            kernel = Seed.createKernel(servletContext, extractInitParameters(servletContext));
            servletContext.setAttribute(KERNEL_ATTRIBUTE_NAME, kernel);
            super.contextInitialized(sce);
        } catch (SeedException e) {
            handleException(e);
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw SeedException.wrap(e, WebErrorCode.UNEXPECTED_EXCEPTION);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        if (kernel != null) {
            try {
                super.contextDestroyed(sce);
                servletContext.removeAttribute(KERNEL_ATTRIBUTE_NAME);
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

    protected Injector getInjector() {
        return kernel.objectGraph().as(Injector.class);
    }

    private Map<String, String> extractInitParameters(ServletContext servletContext) {
        Map<String, String> parameters = new HashMap<String, String>();

        Enumeration<?> initParameterNames = servletContext.getInitParameterNames();
        while (initParameterNames.hasMoreElements()) {
            String parameterName = (String) initParameterNames.nextElement();
            if (parameterName != null && !parameterName.isEmpty()) {
                String parameterValue = servletContext.getInitParameter(parameterName);
                parameters.put(parameterName, parameterValue);
            }
        }

        return parameters;
    }

    private void handleException(Throwable t) {
        LOGGER.error("An exception occurred during web application startup, collecting diagnostic information");
        Seed.diagnostic(kernel).dumpDiagnosticReport(t);
    }
}
