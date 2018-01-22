/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal.websocket;

import java.util.Set;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.web.internal.WebErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WebSocketListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketListener.class);
    private static final String SERVER_CONTAINER = "javax.websocket.server.ServerContainer";

    private final Set<Class<?>> serverEndpointClasses;

    WebSocketListener(Set<Class<?>> serverEndpointClasses) {
        this.serverEndpointClasses = serverEndpointClasses;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServerContainer container = (ServerContainer) sce.getServletContext().getAttribute(SERVER_CONTAINER);
        if (container != null) {
            for (Class<?> endpointClass : serverEndpointClasses) {
                try {
                    LOGGER.trace("Publishing WebSocket server endpoint {}", endpointClass.getCanonicalName());
                    container.addEndpoint(endpointClass);
                } catch (DeploymentException e) {
                    throw SeedException.wrap(e, WebErrorCode.CANNOT_PUBLISH_WEBSOCKET_ENDPOINT)
                            .put("class", endpointClass);
                }
            }
            LOGGER.trace("Published {} WebSocket server endpoint(s)", serverEndpointClasses.size());
        } else {
            LOGGER.info("No WebSocket implementation found, WebSocket support disabled");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing to do
    }
}
