/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import java.util.Collection;
import java.util.Map;
import javax.ws.rs.core.Variant;
import org.seedstack.seed.rest.RestConfig;
import org.seedstack.seed.rest.spi.RootResource;

class RestModule extends AbstractModule {
    private final Collection<Class<?>> resources;
    private final Collection<Class<?>> providers;
    private final RestConfig restConfig;
    private final Map<Variant, Class<? extends RootResource>> rootResourcesByVariant;

    RestModule(RestConfig restConfig, Collection<Class<?>> resources, Collection<Class<?>> providers,
            Map<Variant, Class<? extends RootResource>> rootResourcesByVariant) {
        this.restConfig = restConfig;
        this.rootResourcesByVariant = rootResourcesByVariant;
        this.resources = resources;
        this.providers = providers;
    }

    @Override
    protected void configure() {
        bind(String.class).annotatedWith(Names.named("SeedRestPath")).toInstance(restConfig.getPath());
        bind(String.class).annotatedWith(Names.named("SeedJspPath")).toInstance(restConfig.getJspPath());

        for (Class<?> resource : resources) {
            bind(resource);
        }

        for (Class<?> provider : providers) {
            bind(provider).in(Scopes.SINGLETON);
        }

        if (!rootResourcesByVariant.isEmpty()) {
            MapBinder<Variant, RootResource> multiBinder = MapBinder.newMapBinder(binder(), Variant.class,
                    RootResource.class);
            for (Map.Entry<Variant, Class<? extends RootResource>> rootResourceClassEntry : rootResourcesByVariant
                    .entrySet()) {
                multiBinder.addBinding(rootResourceClassEntry.getKey()).to(rootResourceClassEntry.getValue());
            }
        }
    }
}
