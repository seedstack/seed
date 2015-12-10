/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import org.seedstack.seed.rest.internal.jsonhome.JsonHome;
import org.seedstack.seed.rest.spi.RootResource;

import javax.ws.rs.core.Variant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class RestModule extends ServletModule {
    private final String restPath;
    private final String jspPath;
    private final Map<String, String> jerseyParameters;
    private final Set<Class<? extends ResourceFilterFactory>> resourceFilterFactories;
    private final Collection<Class<?>> resourceClasses;
    private final Collection<Class<?>> providerClasses;
    private final JsonHome jsonHome;
    private final Map<Variant, Class<? extends RootResource>> rootResourceClasses;

    RestModule(Map<Variant, Class<? extends RootResource>> rootResourceClasses, Collection<Class<?>> resourceClasses, Collection<Class<?>> providerClasses, Map<String, String> jerseyParameters,
               Set<Class<? extends ResourceFilterFactory>> resourceFilterFactories, String restPath, String jspPath, JsonHome jsonHome) {
        this.rootResourceClasses = rootResourceClasses;
        this.resourceFilterFactories = resourceFilterFactories;
        this.restPath = restPath;
        this.jspPath = jspPath;
        this.jerseyParameters = jerseyParameters;
        this.resourceClasses = resourceClasses;
        this.providerClasses = providerClasses;
        this.jsonHome = jsonHome;
    }

    @Override
    protected void configureServlets() {
        MapBinder<Variant, RootResource> multiBinder = MapBinder.newMapBinder(binder(), Variant.class, RootResource.class);
        for (Map.Entry<Variant, Class<? extends RootResource>> rootResourceClassEntry : rootResourceClasses.entrySet()) {
            multiBinder.addBinding(rootResourceClassEntry.getKey()).to(rootResourceClassEntry.getValue());
        }

        bind(String.class).annotatedWith(Names.named("SeedRestPath")).toInstance(this.restPath);
        bind(String.class).annotatedWith(Names.named("SeedJspPath")).toInstance(this.jspPath);

        Map<String, String> initParams = new HashMap<String, String>();

        // Default configuration values
        initParams.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
        initParams.put("com.sun.jersey.config.feature.FilterForwardOn404", "true");
        initParams.put("com.sun.jersey.config.feature.DisableWADL", "true");

        // User configuration values
        initParams.putAll(jerseyParameters);

        // Forced configuration values
        initParams.put("com.sun.jersey.config.property.JSPTemplatesBasePath", jspPath);
        initParams.put("com.sun.jersey.config.feature.FilterContextPath", restPath);

        bind(SeedContainer.class).in(Scopes.SINGLETON);
        filter(restPath + "/*").through(SeedContainer.class, initParams);

        Multibinder<ResourceFilterFactory> resourceFilterFactoryMultibinder = Multibinder.newSetBinder(binder(), ResourceFilterFactory.class);
        for (Class<? extends ResourceFilterFactory> resourceFilterFactory : resourceFilterFactories) {
            resourceFilterFactoryMultibinder.addBinding().to(resourceFilterFactory);
        }

        // bind root resource if necessary
        if (!rootResourceClasses.isEmpty()) {
            bind(RootResourceDispatcher.class);
        }

        // bind other resources
        for (Class<?> resourceClass : resourceClasses) {
            if (!RootResourceDispatcher.class.isAssignableFrom(resourceClass)) {
                bind(resourceClass);
            }
        }

        for (Class<?> providerClass : providerClasses) {
            bind(providerClass).in(Scopes.SINGLETON);
        }

        bind(JsonHome.class).toInstance(jsonHome);
    }
}
