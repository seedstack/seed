/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import com.google.inject.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * This module bind all the kind of endpoint defined in the jsr 356 (Endpoint, ClientEndpoint, ServerEnpoint).
 * It also request static injection for the SeedServerEndpointConfigurator.
 *
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 17/12/13
 */
class WebSocketModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketModule.class);
    private final Collection<Class<?>> endpoint;

    WebSocketModule(Collection<Class<?>> endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    protected void configure() {
        requestStaticInjection(SeedServerEndpointConfigurator.class);
        requestStaticInjection(SeedClientEndpointConfigurator.class);

        for (Class<?> aClass : endpoint) {
            LOGGER.debug("Binding WebSocket endpoint {}", aClass);
            bind(aClass);
        }

        LOGGER.info("{} WebSocket endpoint(s) bound", endpoint.size());
    }
}
