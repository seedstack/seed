/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal.websocket;

import com.google.inject.Injector;
import io.nuun.kernel.api.plugin.PluginException;
import java.util.Arrays;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import org.seedstack.seed.web.internal.ServletContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WebSocketServletContextListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServletContextListener.class);

    private final Set<Class<?>> serverEndpointClasses;

    WebSocketServletContextListener(Set<Class<?>> serverEndpointClasses) {
        this.serverEndpointClasses = serverEndpointClasses;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        ServerContainer container = (ServerContainer) servletContext.getAttribute(
                "javax.websocket.server.ServerContainer");
        if (container != null) {
            for (Class<?> endpointClass : serverEndpointClasses) {
                try {
                    LOGGER.trace("Registering WebSocket server endpoint {}", endpointClass.getCanonicalName());
                    ServerEndpoint serverEndpoint = endpointClass.getAnnotation(ServerEndpoint.class);
                    ServerEndpointConfig serverEndpointConfig = ServerEndpointConfig.Builder.create(endpointClass,
                            serverEndpoint.value())
                            .decoders(Arrays.asList(serverEndpoint.decoders()))
                            .encoders(Arrays.asList(serverEndpoint.encoders()))
                            .subprotocols(Arrays.asList(serverEndpoint.subprotocols()))
                            .configurator(getConfiguratorInstance(servletContext, serverEndpoint))
                            .build();
                    container.addEndpoint(serverEndpointConfig);
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

    private ServerEndpointConfig.Configurator getConfiguratorInstance(ServletContext servletContext,
            ServerEndpoint serverEndpoint) {
        Injector injector = ServletContextUtils.getInjector(servletContext);
        if (ServerEndpointConfig.Configurator.class == serverEndpoint.configurator()) {
            return injector.getInstance(SeedServerEndpointConfigurator.class);
        } else {
            return injector.getInstance(serverEndpoint.configurator());
        }
    }
}
