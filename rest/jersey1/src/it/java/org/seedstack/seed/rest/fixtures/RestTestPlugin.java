/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.fixtures;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;
import org.seedstack.seed.rest.internal.RestPlugin;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import java.util.Collection;

public class RestTestPlugin extends AbstractPlugin {
    @Override
    public String name() {
        return "rest-test";
    }

    @Override
    public InitState init(InitContext initContext) {
        RestPlugin restPlugin = initContext.dependency(RestPlugin.class);
        ConfigurationProvider configurationProvider = initContext.dependency(ConfigurationProvider.class);

        if (!configurationProvider.getConfiguration().getBoolean("disable-text-home", false)) {
            restPlugin.registerRootResource(new Variant(MediaType.TEXT_PLAIN_TYPE, null, null), TextRootResource.class);
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(RestPlugin.class, ConfigurationProvider.class);
    }
}
