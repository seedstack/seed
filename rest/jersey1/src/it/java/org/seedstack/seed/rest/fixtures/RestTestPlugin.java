/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.fixtures;

import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.seedstack.seed.rest.internal.RestPlugin;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import java.util.ArrayList;
import java.util.Collection;

public class RestTestPlugin extends AbstractPlugin {
    @Override
    public String name() {
        return "rest-test-plugin";
    }

    @Override
    public InitState init(InitContext initContext) {
        RestPlugin restPlugin = (RestPlugin) initContext.pluginsRequired().iterator().next();

        restPlugin.registerRootResource(new Variant(MediaType.TEXT_HTML_TYPE, null, null), HTMLRootSubResource.class);

        return InitState.INITIALIZED;
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(RestPlugin.class);
        return plugins;
    }
}
