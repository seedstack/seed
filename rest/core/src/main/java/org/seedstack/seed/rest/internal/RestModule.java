/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import java.util.Collection;

class RestModule extends AbstractModule {
    private final Collection<Class<?>> resources;
    private final Collection<Class<?>> providers;
    private final RestConfiguration restConfiguration;

    RestModule(RestConfiguration restConfiguration, Collection<Class<?>> resources, Collection<Class<?>> providers) {
        this.restConfiguration = restConfiguration;
        this.resources = resources;
        this.providers = providers;
    }

    @Override
    protected void configure() {
        bindResources();
        bindProviders();
    }

    private void bindResources() {
        bind(String.class).annotatedWith(Names.named("SeedRestPath")).toInstance(restConfiguration.getRestPath());
        bind(String.class).annotatedWith(Names.named("SeedJspPath")).toInstance(restConfiguration.getJspPath());

        for (Class<?> resource : resources) {
            if (!RootResourceDispatcher.class.isAssignableFrom(resource)) {
                bind(resource);
            }
        }
    }

    private void bindProviders() {
        for (Class<?> provider : providers) {
            bind(provider).in(Scopes.SINGLETON);
        }
    }
}
