/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.internal.web;


import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.sun.xml.ws.transport.http.servlet.WSServlet;

import java.util.List;

class WSServletModule extends ServletModule {
    private final List<String> endpointUrls;

    WSServletModule(List<String> endpointUrls) {
        this.endpointUrls = endpointUrls;
    }

    @Override
    protected void configureServlets() {
        bind(WSServlet.class).in(Scopes.SINGLETON);

        for (String endpointUrl : endpointUrls) {
            serve(endpointUrl).with(WSServlet.class);
        }
    }
}
