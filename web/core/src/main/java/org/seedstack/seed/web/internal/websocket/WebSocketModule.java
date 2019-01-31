/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.websocket;

import com.google.inject.AbstractModule;
import java.util.Set;
import org.seedstack.seed.web.websocket.BaseClientEndpointConfigurator;
import org.seedstack.seed.web.websocket.BaseServerEndpointConfigurator;

class WebSocketModule extends AbstractModule {
    private final Set<Class<?>> serverEndpointClasses;
    private final Set<Class<?>> clientEndpointClasses;

    WebSocketModule(Set<Class<?>> serverEndpointClasses, Set<Class<?>> clientEndpointClasses) {
        this.serverEndpointClasses = serverEndpointClasses;
        this.clientEndpointClasses = clientEndpointClasses;
    }

    @Override
    protected void configure() {
        bind(WebSocketListener.class)
                .toInstance(new WebSocketListener(serverEndpointClasses));

        requestStaticInjection(BaseServerEndpointConfigurator.class);
        requestStaticInjection(BaseClientEndpointConfigurator.class);

        serverEndpointClasses.forEach(this::bind);
        clientEndpointClasses.forEach(this::bind);
    }
}
