/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.websocket;

import com.google.inject.Injector;

import javax.inject.Inject;
import javax.websocket.ClientEndpointConfig;

/**
 * Endpoint configurator that makes WebSocket client endpoints injectable.
 *
 * @author pierre.thirouin@ext.mpsa.com
 */
public class SeedClientEndpointConfigurator extends ClientEndpointConfig.Configurator {
    @Inject
    private static Injector injector;

    /**
     * Creates the endpoint configurator.
     */
    public SeedClientEndpointConfigurator() {
        super();
        injector.injectMembers(this);
    }
}
