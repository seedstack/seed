/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.internal.jms;

import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.TransportTubeFactory;
import com.sun.xml.ws.api.pipe.Tube;

import javax.inject.Inject;

/**
 * Transport tube factory for JMS transport.
 *
 * @author adrien.lauer@mpsa.com
 */
public class JmsTransportTubeFactory extends TransportTubeFactory {
    @Inject
    private static WSJmsTransportFactory wsJmsTransportFactory;

    @Override
    public Tube doCreate(ClientTubeAssemblerContext context) {
        if (context.getAddress().getURI().getScheme().equalsIgnoreCase("jms")) {
            return wsJmsTransportFactory.createJmsTransportTube(context.getCodec(), context.getService().getServiceName());
        }

        return null;
    }

}
