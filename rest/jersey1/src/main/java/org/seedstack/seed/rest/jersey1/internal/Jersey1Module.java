/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey1.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.sun.jersey.spi.container.ResourceFilterFactory;

import java.util.Set;

class Jersey1Module extends AbstractModule {
    private final Set<Class<? extends ResourceFilterFactory>> resourceFilterFactories;

    Jersey1Module(Set<Class<? extends ResourceFilterFactory>> resourceFilterFactories) {
        this.resourceFilterFactories = resourceFilterFactories;
    }

    @Override
    protected void configure() {
        bind(SeedContainer.class).in(Scopes.SINGLETON);

        Multibinder<ResourceFilterFactory> resourceFilterFactoryMultibinder = Multibinder.newSetBinder(binder(), ResourceFilterFactory.class);
        for (Class<? extends ResourceFilterFactory> resourceFilterFactory : resourceFilterFactories) {
            resourceFilterFactoryMultibinder.addBinding().to(resourceFilterFactory);
        }
    }
}
