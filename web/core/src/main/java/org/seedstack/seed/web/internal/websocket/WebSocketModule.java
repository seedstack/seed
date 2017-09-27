/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal.websocket;

import com.google.inject.AbstractModule;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.server.ServerEndpointConfig;

class WebSocketModule extends AbstractModule {
    private final Set<Class<?>> serverEndpointClasses;
    private final Set<Class<?>> clientEndpointClasses;
    private final Set<Class<? extends ServerEndpointConfig.Configurator>> serverConfiguratorClasses;
    private final Set<Class<? extends ClientEndpointConfig.Configurator>> clientConfiguratorClasses;

    WebSocketModule(Set<Class<?>> serverEndpointClasses,
            Set<Class<? extends ServerEndpointConfig.Configurator>> serverConfiguratorClasses,
            Set<Class<?>> clientEndpointClasses,
            HashSet<Class<? extends ClientEndpointConfig.Configurator>> clientConfiguratorClasses) {
        this.serverEndpointClasses = serverEndpointClasses;
        this.serverConfiguratorClasses = serverConfiguratorClasses;
        this.clientEndpointClasses = clientEndpointClasses;
        this.clientConfiguratorClasses = clientConfiguratorClasses;
    }

    @Override
    protected void configure() {
        bind(WebSocketServletContextListener.class).toInstance(
                new WebSocketServletContextListener(serverEndpointClasses));
        bindServerClasses();
        bindClientClasses();
    }

    private void bindServerClasses() {
        bind(SeedServerEndpointConfigurator.class);

        requestStaticInjection(SeedServerEndpointConfigurator.class);

        for (Class<?> serverEndpointClass : serverEndpointClasses) {
            bind(serverEndpointClass);
        }

        for (Class<? extends ServerEndpointConfig.Configurator> serverConfiguratorClass : serverConfiguratorClasses) {
            if (serverConfiguratorClass != SeedServerEndpointConfigurator.class) {
                bind(serverConfiguratorClass);
            }
        }
    }

    private void bindClientClasses() {
        bind(SeedClientEndpointConfigurator.class);

        requestStaticInjection(SeedClientEndpointConfigurator.class);

        for (Class<?> clientEndpointClass : clientEndpointClasses) {
            bind(clientEndpointClass);
        }

        for (Class<? extends ClientEndpointConfig.Configurator> clientConfiguratorClass : clientConfiguratorClasses) {
            if (clientConfiguratorClass != SeedClientEndpointConfigurator.class) {
                bind(clientConfiguratorClass);
            }
        }
    }
}
