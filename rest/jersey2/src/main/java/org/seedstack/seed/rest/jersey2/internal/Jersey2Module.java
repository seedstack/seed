/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.internal;

import com.google.inject.servlet.ServletModule;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;
import org.glassfish.jersey.servlet.ServletProperties;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.rest.internal.RestConcern;
import org.seedstack.seed.rest.internal.RestConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RestConcern
class Jersey2Module extends ServletModule {
    private final RestConfiguration restConfiguration;
    private final Collection<Class<?>> resources;
    private final Collection<Class<?>> providers;

    Jersey2Module(RestConfiguration restConfiguration, Collection<Class<?>> resources, Collection<Class<?>> providers) {
        this.restConfiguration = restConfiguration;
        this.resources = resources;
        this.providers = providers;
    }

    @Override
    protected void configureServlets() {
        Map<String, Object> jerseyProperties = buildJerseyProperties();
        Map<String, String> initParams = buildInitParams(jerseyProperties);

        filter(restConfiguration.getRestPath() + "/*").through(
                new SeedServletContainer(resources, providers, jerseyProperties),
                initParams
        );
    }

    private Map<String, String> buildInitParams(Map<String, Object> jerseyProperties) {
        Map<String, String> initParams = new HashMap<String, String>();

        // Those properties must be defined as init parameters of the filter
        initParams.put(ServletProperties.FILTER_CONTEXT_PATH, (String) jerseyProperties.get(ServletProperties.FILTER_CONTEXT_PATH));
        initParams.put(ServletProperties.FILTER_STATIC_CONTENT_REGEX, (String) jerseyProperties.get(ServletProperties.FILTER_STATIC_CONTENT_REGEX));

        return initParams;
    }

    private Map<String, Object> buildJerseyProperties() {
        Map<String, Object> jerseyProperties = new HashMap<String, Object>();

        // Default configuration values
        jerseyProperties.put(ServletProperties.FILTER_FORWARD_ON_404, true);
        jerseyProperties.put(ServerProperties.WADL_FEATURE_DISABLE, true);

        // User-defined configuration values
        jerseyProperties.putAll(propertiesToMap(restConfiguration.getJerseyProperties()));

        // Forced configuration values
        if (SeedReflectionUtils.forName("org.glassfish.jersey.server.mvc.jsp.JspMvcFeature").isPresent()) {
            jerseyProperties.put(JspMvcFeature.TEMPLATE_BASE_PATH, restConfiguration.getJspPath());
        }
        jerseyProperties.put(ServletProperties.FILTER_CONTEXT_PATH, restConfiguration.getRestPath());

        return jerseyProperties;
    }

    private Map<String, String> propertiesToMap(Properties properties) {
        Map<String, String> map = new HashMap<String, String>();

        for (Object key : properties.keySet()) {
            map.put(key.toString(), properties.getProperty(key.toString()));
        }

        return map;
    }
}
