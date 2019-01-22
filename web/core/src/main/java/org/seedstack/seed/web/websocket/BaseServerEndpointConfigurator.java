/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.websocket;

import com.google.inject.Injector;
import javax.inject.Inject;
import javax.websocket.server.ServerEndpointConfig;

/**
 * This Configurator is used as default configurator for SeedStack ServerEndpoints.
 * It overrides endpoint instantiation to use Guice.
 */
public class BaseServerEndpointConfigurator extends ServerEndpointConfig.Configurator {
    @Inject
    private static Injector injector;

    /**
     * Creates the endpoint configurator.
     */
    public BaseServerEndpointConfigurator() {
        super();
        injector.injectMembers(this);
    }

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return injector.getInstance(endpointClass);
    }
}
