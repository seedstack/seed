/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey1.internal;

import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import org.seedstack.seed.rest.internal.RestConcern;
import org.seedstack.seed.rest.internal.RestConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@RestConcern
class Jersey1Module extends ServletModule {
    private final Set<Class<? extends ResourceFilterFactory>> resourceFilterFactories;
    private final RestConfiguration restConfiguration;

    Jersey1Module(RestConfiguration restConfiguration, Set<Class<? extends ResourceFilterFactory>> resourceFilterFactories) {
        this.restConfiguration = restConfiguration;
        this.resourceFilterFactories = resourceFilterFactories;
    }

    @Override
    protected void configureServlets() {
        Map<String, String> initParams = new HashMap<String, String>();

        // Default configuration values
        initParams.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
        initParams.put("com.sun.jersey.config.feature.FilterForwardOn404", "true");
        initParams.put("com.sun.jersey.config.feature.DisableWADL", "true");

        // User configuration values
        initParams.putAll(propertiesToMap(restConfiguration.getJerseyProperties()));

        // Forced configuration values
        initParams.put("com.sun.jersey.config.property.JSPTemplatesBasePath", restConfiguration.getJspPath());
        initParams.put("com.sun.jersey.config.feature.FilterContextPath", restConfiguration.getRestPath());

        bind(SeedContainer.class).in(Scopes.SINGLETON);
        filter(restConfiguration.getRestPath() + "/*").through(SeedContainer.class, initParams);

        Multibinder<ResourceFilterFactory> resourceFilterFactoryMultibinder = Multibinder.newSetBinder(binder(), ResourceFilterFactory.class);
        for (Class<? extends ResourceFilterFactory> resourceFilterFactory : resourceFilterFactories) {
            resourceFilterFactoryMultibinder.addBinding().to(resourceFilterFactory);
        }
    }

    private Map<String, String> propertiesToMap(Properties properties) {
        Map<String, String> map = new HashMap<String, String>();

        for (Object key : properties.keySet()) {
            map.put(key.toString(), properties.getProperty(key.toString()));
        }

        return map;
    }
}
