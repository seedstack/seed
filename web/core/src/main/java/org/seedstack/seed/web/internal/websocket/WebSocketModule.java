/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.websocket;

import com.google.inject.AbstractModule;

import java.util.Set;

/**
 * This module bind all the kind of endpoint defined in the jsr 356 (Endpoint, ClientEndpoint, ServerEnpoint).
 * It also request static injection for the SeedServerEndpointConfigurator.
 *
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 17/12/13
 */
class WebSocketModule extends AbstractModule {
    private final Set<Class<?>> serverEndpointClasses;

    WebSocketModule(Set<Class<?>> serverEndpointClasses) {
        this.serverEndpointClasses = serverEndpointClasses;
    }

    @Override
    protected void configure() {
        bind(WebSocketServletContextListener.class).toInstance(new WebSocketServletContextListener(serverEndpointClasses));

        requestStaticInjection(SeedServerEndpointConfigurator.class);
        requestStaticInjection(SeedClientEndpointConfigurator.class);

        for (Class<?> aClass : serverEndpointClasses) {
            bind(aClass);
        }
    }
}
