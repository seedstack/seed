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
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.web.DelegateServletContextListener;
import org.seedstack.seed.web.internal.WebErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * This context listener has the responsibility to initialize the Seed framework in a web environment and
 * to initialize/destroy any delegate context listener marked with the {@link DelegateServletContextListener} interface.
 *
 * @author yves.dautremay@mpsa.com
 * @author epo.jemba@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 */
public class SeedServletContextListener extends GuiceServletContextListener implements ServletContextListener, ServletContextAttributeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeedServletContextListener.class);

    private final DelegateServletContextListenerImpl delegateListeners;
    private Kernel kernel;
    private Injector injector;

    /**
     * Creates the context listener.
     */
    public SeedServletContextListener() {
        ServiceLoader<DelegateServletContextListener> loader = ServiceLoader.load(DelegateServletContextListener.class, SeedReflectionUtils.findMostCompleteClassLoader(SeedServletContextListener.class));
        Iterator<DelegateServletContextListener> iterator = loader.iterator();

        List<ServletContextListener> listeners = new ArrayList<ServletContextListener>();

        while (iterator.hasNext()) {
            listeners.add(iterator.next());
        }

        delegateListeners = new DelegateServletContextListenerImpl(listeners);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Starting Seed Web application");

        try {
            kernel = createKernel(sce.getServletContext());
            kernel.init();
            kernel.start();
            injector = kernel.objectGraph().as(Injector.class);

            super.contextInitialized(sce);

            delegateListeners.setCallback(new AbstractCallback() {
                @Override
                public void onBeforeContextInitialized(ServletContextListener candidate) {
                    injector.injectMembers(candidate);
                }
            });
            delegateListeners.contextInitialized(sce);
        } catch (SeedException e) {
            handleException(e);
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw SeedException.wrap(e, WebErrorCode.UNEXPECTED_EXCEPTION);
        }

        // java.lang.Error handling is delegated to the container

        LOGGER.info("Seed Web application started");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Stopping Seed Web application");

        try {
            delegateListeners.contextDestroyed(sce);

            if (kernel != null && kernel.isStarted()) {
                kernel.stop();
            }
        } catch (SeedException e) {
            handleException(e);
            throw e;
        } catch (Exception e) {
            handleException(e);
            throw SeedException.wrap(e, WebErrorCode.UNEXPECTED_EXCEPTION);
        }

        // java.lang.Error handling is delegated to the container

        LOGGER.info("Seed Web application stopped");
    }

    @Override
    protected Injector getInjector() {
        return injector;
    }

    @Override
    public void attributeAdded(ServletContextAttributeEvent scab) {
        delegateListeners.attributeAdded(scab);
    }

    @Override
    public void attributeRemoved(ServletContextAttributeEvent scab) {
        delegateListeners.attributeRemoved(scab);
    }

    @Override
    public void attributeReplaced(ServletContextAttributeEvent scab) {
        delegateListeners.attributeReplaced(scab);
    }

    private void handleException(Throwable t) {
        LOGGER.error("An exception occurred during web application startup, collecting diagnostic information");
        CorePlugin.getDiagnosticManager().dumpDiagnosticReport(t);
    }

    private Kernel createKernel(ServletContext servletContext) {
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

    abstract class AbstractCallback {
        abstract void onBeforeContextInitialized(ServletContextListener candidate);
    }
}
