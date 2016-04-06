/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import com.google.inject.Injector;
import org.seedstack.seed.web.FilterDefinition;
import org.seedstack.seed.web.ListenerDefinition;
import org.seedstack.seed.web.ServletDefinition;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

class ServletContextConfigurer {
    private final ServletContext servletContext;
    private final Injector injector;

    ServletContextConfigurer(ServletContext servletContext, Injector injector) {
        this.servletContext = servletContext;
        this.injector = injector;
    }

    void addFilter(FilterDefinition filterDefinition) {
        FilterRegistration.Dynamic filterRegistration = servletContext.addFilter(
                filterDefinition.getName(),
                injector.getInstance(filterDefinition.getFilterClass())
        );
        filterRegistration.setAsyncSupported(filterDefinition.isAsyncSupported());
        for (FilterDefinition.Mapping mapping : filterDefinition.getServletMappings()) {
            filterRegistration.addMappingForServletNames(mapping.getDispatcherTypes(), mapping.isMatchAfter(), mapping.getValues());
        }
        for (FilterDefinition.Mapping mapping : filterDefinition.getMappings()) {
            filterRegistration.addMappingForUrlPatterns(mapping.getDispatcherTypes(), mapping.isMatchAfter(), mapping.getValues());
        }
        filterRegistration.setInitParameters(filterDefinition.getInitParameters());
    }

    void addServlet(ServletDefinition servletDefinition) {
        ServletRegistration.Dynamic servletRegistration = servletContext.addServlet(
                servletDefinition.getName(),
                injector.getInstance(servletDefinition.getServletClass())
        );
        servletRegistration.setAsyncSupported(servletDefinition.isAsyncSupported());
        for (String mapping : servletDefinition.getMappings()) {
            servletRegistration.addMapping(mapping);
        }
        servletRegistration.setLoadOnStartup(servletDefinition.getLoadOnStartup());
        servletRegistration.setInitParameters(servletDefinition.getInitParameters());
    }

    void addListener(ListenerDefinition listenerDefinition) {
        servletContext.addListener(injector.getInstance(listenerDefinition.getListenerClass()));
    }
}
