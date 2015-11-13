/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.servlet.ServletModule;
import org.seedstack.seed.web.WebResourceResolver;
import org.seedstack.seed.web.WebResourceResolverFactory;

import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

@WebConcern
class WebModule extends ServletModule {
    private final List<ConfiguredServlet> servlets;
    private final List<ConfiguredFilter> filters;
    private final boolean resourcesEnabled;
    private final Set<ServletModule> additionalModules;
    private final boolean requestDiagnosticEnabled;

    WebModule(boolean requestDiagnosticEnabled,
              List<ConfiguredServlet> servlets,
              List<ConfiguredFilter> filters,
              boolean resourcesEnabled,
              Set<ServletModule> additionalModules) {
        this.requestDiagnosticEnabled = requestDiagnosticEnabled;
        this.servlets = servlets;
        this.filters = filters;
        this.resourcesEnabled = resourcesEnabled;
        this.additionalModules = additionalModules;
    }

    @Override
    protected void configureServlets() {
        // Diagnostic
        if (requestDiagnosticEnabled) {
            bind(ExceptionDiagnosticFilter.class).in(Singleton.class);
            filter("/*").through(ExceptionDiagnosticFilter.class);
        }

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

        // Additional web modules
        for (ServletModule additionalModule : additionalModules) {
            install(additionalModule);
        }

        // Static resources serving
        if (resourcesEnabled) {
            install(new FactoryModuleBuilder()
                    .implement(WebResourceResolver.class, WebResourceResolverImpl.class)
                    .build(WebResourceResolverFactory.class)
            );

            bind(WebResourceFilter.class).in(Singleton.class);
            filter("/*").through(WebResourceFilter.class);
        }
    }
}
