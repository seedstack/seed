/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.cors;

import com.google.inject.servlet.ServletModule;
import com.thetransactioncompany.cors.CORSFilter;

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
