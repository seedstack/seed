/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import com.google.inject.servlet.GuiceFilter;
import org.seedstack.seed.web.listener.SeedServletContextListener;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Set;

public class SeedServletContainerInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> servletContextConfigurerClasses, ServletContext servletContext) throws ServletException {
        configureGuiceFilter(servletContext);
        servletContext.addListener(SeedServletContextListener.class);
    }

    private void configureGuiceFilter(ServletContext ctx) {
        boolean guiceFilterAlreadyRegistered = false;

        for (FilterRegistration filterRegistration : ctx.getFilterRegistrations().values()) {
            if (GuiceFilter.class.getName().equals(filterRegistration.getClassName())) {
                guiceFilterAlreadyRegistered = true;
                break;
            }
        }

        if (!guiceFilterAlreadyRegistered) {
            FilterRegistration.Dynamic guiceFilter = ctx.addFilter("guiceFilter", GuiceFilter.class);
            if (guiceFilter != null) {
                guiceFilter.addMappingForUrlPatterns(null, false, "/*");
            }
        } else {
            ctx.log("Guice filter already registered, avoiding automatic registration");
        }
    }
}
