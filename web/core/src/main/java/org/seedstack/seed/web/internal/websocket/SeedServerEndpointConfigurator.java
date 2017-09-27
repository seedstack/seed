/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal.websocket;

import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.websocket.Extension;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * This Configurator is used as default configurator for Seed ServerEndpoints.
 * It overrides endpoint instantiation to use Guice.
 */
public class SeedServerEndpointConfigurator extends ServerEndpointConfig.Configurator {
    @Inject
    private Injector injector;

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return injector.getInstance(endpointClass);
    }

    @Override
    public String getNegotiatedSubprotocol(List<String> supported, List<String> requested) {
        for (String request : requested) {
            if (supported.contains(request)) {
                return request;
            }
        }
        return "";
    }

    @Override
    public List<Extension> getNegotiatedExtensions(List<Extension> installed, List<Extension> requested) {
        List<Extension> result = new ArrayList<>();
        for (Extension request : requested) {
            if (installed.contains(request)) {
                result.add(request);
            }
        }
        return result;
    }

    @Override
    public boolean checkOrigin(String originHeaderValue) {
        return true;
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        // NO-OP
    }
}
