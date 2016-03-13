/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.resources;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.SeedRuntime;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;
import org.seedstack.seed.web.internal.WebPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.util.Collection;

/**
 * This plugin provides static Web resource serving.
 *
 * @author adrien.lauer@mpsa.com
 */
public class WebResourcePlugin extends AbstractPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebResourcePlugin.class);

    private ServletContext servletContext;
    private WebResourceModule webResourceModule;

    @Override
    public String name() {
        return "web-resources";
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(ConfigurationProvider.class);
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        servletContext = ((SeedRuntime) containerContext).contextAs(ServletContext.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState init(InitContext initContext) {
        if (servletContext == null) {
            LOGGER.info("No servlet context detected, static web resources disabled");
            return InitState.INITIALIZED;
        }

        Configuration webConfiguration = initContext.dependency(ConfigurationProvider.class).getConfiguration().subset(WebPlugin.WEB_PLUGIN_PREFIX);
        if (webConfiguration.getBoolean("resources.enabled", true)) {
            LOGGER.info("Static resources served on /");
            webResourceModule = new WebResourceModule();
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Module nativeUnitModule() {
        return webResourceModule;
    }

}
