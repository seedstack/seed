/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.websocket;

import io.nuun.kernel.api.plugin.PluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.websocket.server.ServerContainer;
import java.util.Set;

class WebSocketServletContextListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServletContextListener.class);

    private final Set<Class<?>> serverEndpointClasses;

    WebSocketServletContextListener(Set<Class<?>> serverEndpointClasses) {
        this.serverEndpointClasses = serverEndpointClasses;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServerContainer container = (ServerContainer) sce.getServletContext().getAttribute("javax.websocket.server.ServerContainer");
        if (container != null) {
            for (Class<?> endpointClass : serverEndpointClasses) {
                try {
                    container.addEndpoint(endpointClass);
                    LOGGER.trace("Registering WebSocket server endpoint {}", endpointClass.getCanonicalName());
                } catch (Exception e) {
                    throw new PluginException("Unable to deploy WebSocket server endpoint " + endpointClass, e);
                }
            }
            LOGGER.debug("Registered {} WebSocket server endpoint(s)", serverEndpointClasses.size());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing to do
    }
}
