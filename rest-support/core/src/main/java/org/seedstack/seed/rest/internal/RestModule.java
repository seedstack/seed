/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.spi.container.ResourceFilterFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class RestModule extends ServletModule {
    private final String restPath;
    private final String jspPath;
    private final Set<Class<? extends ResourceFilterFactory>> resourceFilterFactories;
    private final Collection<Class<?>> resourceClasses;
    private final Collection<Class<?>> providerClasses;
    private final Collection<Class<?>> activityClasses;

    RestModule(Collection<Class<?>> resourceClasses, Collection<Class<?>> providerClasses, Collection<Class<?>> activityClasses, Set<Class<? extends ResourceFilterFactory>> resourceFilterFactories, String restPath, String jspPath) {
        this.resourceFilterFactories = resourceFilterFactories;
        this.restPath = restPath;
        this.jspPath = jspPath;
        this.resourceClasses = resourceClasses;
        this.providerClasses = providerClasses;
        this.activityClasses = activityClasses;
    }

    @Override
    protected void configureServlets() {
        bind(String.class).annotatedWith(Names.named("SeedRestPath")).toInstance(this.restPath);
        bind(String.class).annotatedWith(Names.named("SeedJspPath")).toInstance(this.jspPath);

        Map<String, String> initParams = new HashMap<String, String>();

        initParams.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
        initParams.put("com.sun.jersey.config.property.JSPTemplatesBasePath", jspPath);
        initParams.put("com.sun.jersey.config.feature.FilterForwardOn404", "true");
        initParams.put("com.sun.jersey.config.feature.FilterContextPath", restPath);

        bind(SeedContainer.class).in(Scopes.SINGLETON);
        filter(restPath + "/*").through(SeedContainer.class, initParams);

        Multibinder<ResourceFilterFactory> resourceFilterFactoryMultibinder = Multibinder.newSetBinder(binder(), ResourceFilterFactory.class);
        for (Class<? extends ResourceFilterFactory> resourceFilterFactory : resourceFilterFactories) {
            resourceFilterFactoryMultibinder.addBinding().to(resourceFilterFactory);
        }

        for (Class<?> activityClass : activityClasses) {
            bind(activityClass);
        }

        for (Class<?> resourceClass : resourceClasses) {
            bind(resourceClass);
        }

        for (Class<?> providerClass : providerClasses) {
            bind(providerClass).in(Scopes.SINGLETON);
        }
    }
}
