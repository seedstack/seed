/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.listener;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.core.NuunCore;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CorePlugin;
import org.seedstack.seed.web.internal.WebErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * This context listener has the responsibility to initialize the Seed framework in a Web environment. This listener
 * should only be declared directly in a web.xml file when under a Servlet 2.5 level container. Servlet 3+ capable containers
 * will automatically initialize Seed.
 *
 * @author adrien.lauer@mpsa.com
 */
public class SeedServletContextListener extends GuiceServletContextListener {
    private static final String KERNEL_ATTRIBUTE_NAME = Kernel.class.getName();
    private static final Logger LOGGER = LoggerFactory.getLogger(SeedServletContextListener.class);

    private Kernel kernel;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        if (servletContext.getAttribute(KERNEL_ATTRIBUTE_NAME) != null) {
            throw new RuntimeException(
                    SeedException.createNew(WebErrorCode.SEED_ALREADY_INITIALIZED)
                            .put("servlet-context-name", String.valueOf(servletContext.getServletContextName()))
                            .toString()
            );
        }

        LOGGER.info("Starting Seed Web application");

        try {
            kernel = createKernel(servletContext);
            servletContext.setAttribute(KERNEL_ATTRIBUTE_NAME, kernel);
            kernel.init();
            kernel.start();
            super.contextInitialized(sce);
        } catch (SeedException e) {
            handleException(e);
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw SeedException.wrap(e, WebErrorCode.UNEXPECTED_EXCEPTION);
        }

        LOGGER.info("Seed Web application started");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        if (kernel != null) {
            LOGGER.info("Stopping Seed Web application");

            try {
                super.contextDestroyed(sce);
                if (kernel.isStarted()) {
                    kernel.stop();
                }
                servletContext.removeAttribute(KERNEL_ATTRIBUTE_NAME);
            } catch (SeedException e) {
                handleException(e);
                throw e;
            } catch (Exception e) {
                handleException(e);
                throw SeedException.wrap(e, WebErrorCode.UNEXPECTED_EXCEPTION);
            }

            LOGGER.info("Seed Web application stopped");
        }
    }

    @Override
    protected Injector getInjector() {
        return kernel.objectGraph().as(Injector.class);
    }

    private static void handleException(Throwable t) {
        LOGGER.error("An exception occurred during web application startup, collecting diagnostic information");
        CorePlugin.getDiagnosticManager().dumpDiagnosticReport(t);
    }

    private static Kernel createKernel(ServletContext servletContext) {
        List<String> params = new ArrayList<String>();
        Enumeration<?> initparams = servletContext.getInitParameterNames();
        while (initparams.hasMoreElements()) {
            String keyName = (String) initparams.nextElement();
            if (keyName != null && !keyName.isEmpty()) {
                String value = servletContext.getInitParameter(keyName);
                LOGGER.debug("Setting kernel parameter {} to {}", keyName, value);
                params.add(keyName);
                params.add(value);
            }
        }

        return NuunCore.createKernel(NuunCore.newKernelConfiguration().containerContext(servletContext).params(params.toArray(new String[params.size()])));
    }
}
