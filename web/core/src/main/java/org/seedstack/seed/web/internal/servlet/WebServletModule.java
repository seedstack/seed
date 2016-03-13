/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.servlet;

import com.google.inject.servlet.ServletModule;
import org.seedstack.seed.web.WebServlet;

import javax.inject.Singleton;
import java.util.List;

@WebServlet
class WebServletModule extends ServletModule {
    private final List<ConfiguredServlet> servlets;
    private final List<ConfiguredFilter> filters;

    WebServletModule(List<ConfiguredServlet> servlets, List<ConfiguredFilter> filters) {
        this.servlets = servlets;
        this.filters = filters;
    }

    @Override
    protected void configureServlets() {
        // User filters
        for (ConfiguredFilter configuredFilter : filters) {
            bind(configuredFilter.getClazz()).in(Singleton.class);
            for (String urlPattern : configuredFilter.getUrlPatterns()) {
                filter(urlPattern).through(configuredFilter.getClazz(), configuredFilter.getInitParams());
            }
        }

        // User servlets
        for (ConfiguredServlet configuredServlet : servlets) {
            bind(configuredServlet.getClazz()).in(Singleton.class);
            for (String urlPattern : configuredServlet.getUrlPatterns()) {
                serve(urlPattern).with(configuredServlet.getClazz(), configuredServlet.getInitParams());
            }
        }
    }
}
