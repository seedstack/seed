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
import javax.websocket.Extension;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.ArrayList;
import java.util.List;

/**
 * This Configurator is used as default configurator for seed ServerEndpoints.
 * It overrides endpoint instantiation to use Guice.
 *
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 18/12/13
 */
public class SeedServerEndpointConfigurator extends ServerEndpointConfig.Configurator {
    @Inject
    private static Injector injector;

    /**
     * Creates the endpoint configurator.
     */
    public SeedServerEndpointConfigurator() {
        super();
        injector.injectMembers(this);
    }

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
        List<Extension> result = new ArrayList<Extension>();
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
