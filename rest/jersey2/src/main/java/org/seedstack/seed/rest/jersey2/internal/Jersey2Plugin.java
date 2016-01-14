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
import org.seedstack.seed.web.internal.WebPlugin;

import java.util.Collection;

public class Jersey2Plugin extends AbstractPlugin {
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
        return Lists.<Class<?>>newArrayList(WebPlugin.class, RestPlugin.class);
    }

    @Override
    public InitState init(InitContext initContext) {
        RestPlugin restPlugin = initContext.dependency(RestPlugin.class);

        if (restPlugin.isEnabled()) {
            initContext.dependency(WebPlugin.class).registerAdditionalModule(
                    new Jersey2Module(
                            restPlugin.getConfiguration(),
                            restPlugin.getResources(),
                            restPlugin.getProviders()
                    )
            );
        }

        return InitState.INITIALIZED;
    }
}
