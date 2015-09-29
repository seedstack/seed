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

import com.google.inject.servlet.ServletModule;
import com.thetransactioncompany.cors.CORSFilter;
import org.seedstack.seed.web.spi.CorsConcern;

import javax.inject.Singleton;
import java.util.Map;

@CorsConcern
class WebCorsModule extends ServletModule {
    private final String corsMapping;
    private final Map<String, String> corsParameters;

    WebCorsModule(String corsMapping, Map<String, String> corsParameters) {
        this.corsMapping = corsMapping;
        this.corsParameters = corsParameters;
    }

    @Override
    protected void configureServlets() {
        bind(CORSFilter.class).in(Singleton.class);
        filter(corsMapping).through(CORSFilter.class, corsParameters);
    }
}
