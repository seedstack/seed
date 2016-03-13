/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.internal;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.seedstack.seed.rest.internal.RestPlugin;
import org.seedstack.seed.rest.spi.RestProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Jersey2Plugin extends AbstractPlugin {
    private Jersey2Module jersey2Module;

    @Override
    public String name() {
        return "jersey2";
    }

    @Override
    public String pluginPackageRoot() {
        // This is required to detect Jackson providers
        return "com.fasterxml.jackson.jaxrs";
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(RestPlugin.class, RestProvider.class);
    }

    @Override
    public InitState init(InitContext initContext) {
        RestPlugin restPlugin = initContext.dependency(RestPlugin.class);

        if (restPlugin.isEnabled()) {
            List<RestProvider> restProviders = initContext.dependencies(RestProvider.class);
            Set<Class<?>> resources = new HashSet<Class<?>>();
            Set<Class<?>> providers = new HashSet<Class<?>>();
            for (RestProvider restProvider : restProviders) {
                resources.addAll(restProvider.resources());
                providers.addAll(restProvider.providers());
            }

            jersey2Module = new Jersey2Module(restPlugin.getConfiguration(), resources, providers);
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return jersey2Module;
    }
}
