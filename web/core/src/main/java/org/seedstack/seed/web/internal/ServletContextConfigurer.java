/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import com.google.inject.Injector;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import org.seedstack.seed.web.spi.FilterDefinition;
import org.seedstack.seed.web.spi.ListenerDefinition;
import org.seedstack.seed.web.spi.ServletDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ServletContextConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletContextConfigurer.class);
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

        if (filterRegistration != null) {
            filterRegistration.setAsyncSupported(filterDefinition.isAsyncSupported());
            for (FilterDefinition.Mapping mapping : filterDefinition.getServletMappings()) {
                filterRegistration.addMappingForServletNames(mapping.getDispatcherTypes(), mapping.isMatchAfter(),
                        mapping.getValues());
            }
            for (FilterDefinition.Mapping mapping : filterDefinition.getMappings()) {
                filterRegistration.addMappingForUrlPatterns(mapping.getDispatcherTypes(), mapping.isMatchAfter(),
                        mapping.getValues());
            }
            filterRegistration.setInitParameters(filterDefinition.getInitParameters());
        } else {
            LOGGER.warn(
                    "Servlet filter {} was already registered by the container: injection and interception are not "
                            + "available. Consider adding a web.xml file with metadata-complete=true.",
                    filterDefinition.getName());
        }
    }

    void addServlet(ServletDefinition servletDefinition) {
        ServletRegistration.Dynamic servletRegistration = servletContext.addServlet(
                servletDefinition.getName(),
                injector.getInstance(servletDefinition.getServletClass())
        );

        if (servletRegistration != null) {
            servletRegistration.setAsyncSupported(servletDefinition.isAsyncSupported());
            for (String mapping : servletDefinition.getMappings()) {
                servletRegistration.addMapping(mapping);
            }
            servletRegistration.setLoadOnStartup(servletDefinition.getLoadOnStartup());
            servletRegistration.setInitParameters(servletDefinition.getInitParameters());
        } else {
            LOGGER.warn(
                    "Servlet {} was already registered by the container: injection and interception are not available"
                            + ". Consider adding a web.xml file with metadata-complete=true.",
                    servletDefinition.getName());
        }
    }

    void addListener(ListenerDefinition listenerDefinition) {
        servletContext.addListener(injector.getInstance(listenerDefinition.getListenerClass()));
    }
}
