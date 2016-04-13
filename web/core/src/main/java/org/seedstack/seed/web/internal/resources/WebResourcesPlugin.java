/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.resources;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;
import org.seedstack.seed.web.spi.FilterDefinition;
import org.seedstack.seed.web.spi.ListenerDefinition;
import org.seedstack.seed.web.spi.ServletDefinition;
import org.seedstack.seed.web.spi.WebProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

import static org.seedstack.seed.web.internal.WebPlugin.WEB_PLUGIN_PREFIX;

/**
 * This plugin serves static resources under META-INF/resources with several benefits over the default servlet.
 *
 * @see WebResourcesFilter
 */
public class WebResourcesPlugin extends AbstractPlugin implements WebProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebResourcesPlugin.class);
    private boolean webResourcesEnabled;

    @Override
    public String name() {
        return "web-resources";
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(ConfigurationProvider.class);
    }

    @Override
    public InitState init(InitContext initContext) {
        webResourcesEnabled = initContext
                .dependency(ConfigurationProvider.class)
                .getConfiguration()
                .subset(WEB_PLUGIN_PREFIX)
                .getBoolean("resources.enabled", true);

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        if (webResourcesEnabled) {
            return new WebResourcesModule();
        } else {
            return null;
        }
    }

    @Override
    public List<ServletDefinition> servlets() {
        return null;
    }

    @Override
    public List<FilterDefinition> filters() {
        if (webResourcesEnabled) {
            LOGGER.info("Static Web resources served on /*");

            FilterDefinition resourcesFilter = new FilterDefinition("web-resources", WebResourcesFilter.class);
            resourcesFilter.setPriority(-1000);
            resourcesFilter.setAsyncSupported(true);
            resourcesFilter.addMappings(new FilterDefinition.Mapping("/*"));
            return Lists.newArrayList(resourcesFilter);
        } else {
            return null;
        }
    }

    @Override
    public List<ListenerDefinition> listeners() {
        return null;
    }
}
