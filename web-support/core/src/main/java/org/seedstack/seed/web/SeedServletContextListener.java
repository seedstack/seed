/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web;

import com.google.inject.Injector;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.CorePlugin;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.web.api.DelegateServletContextListener;
import org.seedstack.seed.web.internal.WebErrorCode;
import io.nuun.plugin.web.NuunServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;


/**
 * This context listener has the responsibility to initialize the SEED framework in a web environment and
 * to initialize/destroy any delegate context listener marked with the {@link org.seedstack.seed.web.api.DelegateServletContextListener} interface.
 *
 * @author yves.dautremay@mpsa.com
 * @author epo.jemba@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 */
public class SeedServletContextListener implements ServletContextListener, ServletContextAttributeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeedServletContextListener.class);

    private final NuunServletContextListener mainListener;
    private final DelegateServletContextListenerImpl otherListeners;

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

        mainListener = new NuunServletContextListener();
        otherListeners = new DelegateServletContextListenerImpl(listeners);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Starting SEED web application");

        try {
            mainListener.contextInitialized(sce);
            final Injector injector = (Injector) sce.getServletContext().getAttribute(Injector.class.getName());

            otherListeners.setCallback(new AbstractCallback() {
                @Override
                public void onBeforeContextInitialized(ServletContextListener candidate) {
                    injector.injectMembers(candidate);
                }
            });
            otherListeners.contextInitialized(sce);
        } catch (Exception e) { // NOSONAR
            LOGGER.error("An unexpected error occurred during web application startup, collecting diagnostic information");
            CorePlugin.getDiagnosticManager().dumpDiagnosticReport(e);
            throw SeedException.wrap(e, WebErrorCode.UNEXPECTED_WEB_EXCEPTION);
        }

        LOGGER.info("SEED web application started");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Stopping SEED web application");

        try {
            otherListeners.contextDestroyed(sce);
            mainListener.contextDestroyed(sce);
        } catch (Exception e) { // NOSONAR
            LOGGER.error("An unexpected error occurred during web application shutdown, collecting diagnostic information");
            CorePlugin.getDiagnosticManager().dumpDiagnosticReport(e);
            throw SeedException.wrap(e, WebErrorCode.UNEXPECTED_WEB_EXCEPTION);
        }

        LOGGER.info("SEED web application stopped");
    }

    @Override
    public void attributeAdded(ServletContextAttributeEvent scab) {
        otherListeners.attributeAdded(scab);
    }

    @Override
    public void attributeRemoved(ServletContextAttributeEvent scab) {
        otherListeners.attributeRemoved(scab);
    }

    @Override
    public void attributeReplaced(ServletContextAttributeEvent scab) {
        otherListeners.attributeReplaced(scab);
    }

    abstract class AbstractCallback {
        abstract void onBeforeContextInitialized(ServletContextListener candidate);
    }
}
