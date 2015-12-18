/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.util.Collection;

class RestModule extends AbstractModule {

    private final Collection<Class<?>> resources;
    private final Collection<Class<?>> providers;

    public RestModule(Collection<Class<?>> resources, Collection<Class<?>> providers) {
        this.resources = resources;
        this.providers = providers;
    }

    @Override
    protected void configure() {
        bindResources();
        bindProviders();
    }

    private void bindResources() {
        for (Class<?> resource : resources) {
            bind(resource);
        }
    }

    private void bindProviders() {
        for (Class<?> provider : providers) {
            bind(provider).in(Scopes.SINGLETON);
        }
    }
}
