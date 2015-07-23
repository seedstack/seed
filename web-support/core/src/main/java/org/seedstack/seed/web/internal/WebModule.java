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

import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import org.seedstack.seed.web.api.WebResourceResolver;
import org.seedstack.seed.web.spi.WebConcern;

import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

@WebConcern
class WebModule extends ServletModule {
    private final List<ConfiguredServlet> servlets;
    private final List<ConfiguredFilter> filters;
    private final String resourcesPrefix;
    private final boolean resourcesEnabled;
    private final Set<ServletModule> additionalModules;
    private final boolean requestDiagnosticEnabled;

    WebModule(boolean requestDiagnosticEnabled,
              List<ConfiguredServlet> servlets,
              List<ConfiguredFilter> filters,
              boolean resourcesEnabled,
              String resourcesPrefix,
              Set<ServletModule> additionalModules) {
        this.requestDiagnosticEnabled = requestDiagnosticEnabled;
        this.servlets = servlets;
        this.filters = filters;
        this.resourcesEnabled = resourcesEnabled;
        this.resourcesPrefix = resourcesPrefix;
        this.additionalModules = additionalModules;
    }

    @Override
    protected void configureServlets() {
        bind(String.class).annotatedWith(Names.named("SeedWebResourcesPath")).toInstance(this.resourcesPrefix);
        bind(WebResourceResolver.class).to(WebResourceResolverImpl.class);

        // Diagnostic (highest dispatching priority)
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

        // Additional web modules (with lower dispatching priority)
        for (ServletModule additionalModule : additionalModules) {
            install(additionalModule);
        }

        // Static resources filter (with the lowest dispatching priority)
        if (resourcesEnabled) {
            bind(WebResourceFilter.class).in(Singleton.class);
            filter(resourcesPrefix + "/*").through(WebResourceFilter.class);
        }
    }
}
