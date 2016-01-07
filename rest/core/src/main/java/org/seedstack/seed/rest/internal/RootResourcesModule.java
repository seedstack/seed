/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import org.seedstack.seed.rest.spi.RootResource;

import javax.ws.rs.core.Variant;
import java.util.Map;

class RootResourcesModule extends AbstractModule {
    private final Map<Variant, Class<? extends RootResource>> rootResourcesByVariant;

    RootResourcesModule(Map<Variant, Class<? extends RootResource>> rootResourcesByVariant) {
        this.rootResourcesByVariant = rootResourcesByVariant;
    }

    @Override
    protected void configure() {
        bind(RootResourceDispatcher.class);

        MapBinder<Variant, RootResource> multiBinder = MapBinder.newMapBinder(binder(), Variant.class, RootResource.class);
        for (Map.Entry<Variant, Class<? extends RootResource>> rootResourceClassEntry : rootResourcesByVariant.entrySet()) {
            multiBinder.addBinding(rootResourceClassEntry.getKey()).to(rootResourceClassEntry.getValue());
        }
    }
}
