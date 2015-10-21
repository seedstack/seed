/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;
import org.seedstack.seed.core.utils.SeedConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This plugin provides CORS handling. The module returned by this plugin is ordered before the security module so the
 * filter gets the chance to respond to pre-flight OPTIONS requests without being blocked. Other request types are
 * forwarded through the filter chain and are secured as usual.
 *
 * @author adrien.lauer@mpsa.com
 */
public class WebCorsPlugin extends AbstractPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebCorsPlugin.class);

    private ServletContext servletContext;
    private WebCorsModule webCorsModule;

    @Override
    public String name() {
        return "seed-web-cors-plugin";
    }

    @Override
    public InitState init(InitContext initContext) {
        if (this.servletContext == null) {
            LOGGER.info("No servlet context detected, web support disabled");
            return InitState.INITIALIZED;
        }

        Configuration webConfiguration = initContext.dependency(ConfigurationProvider.class).getConfiguration().subset(WebPlugin.WEB_PLUGIN_PREFIX);

        boolean corsEnabled = webConfiguration.getBoolean("cors.enabled", false);
        Map<String, String> corsParameters = new HashMap<String, String>();
        String corsMapping;
        if (corsEnabled) {
            corsMapping = webConfiguration.getString("cors.url-mapping", "/*");

            Properties corsProperties = SeedConfigurationUtils.buildPropertiesFromConfiguration(webConfiguration, "cors.property");
            for (Object key : corsProperties.keySet()) {
                corsParameters.put("cors." + key.toString(), corsProperties.getProperty(key.toString()));
            }

            webCorsModule = new WebCorsModule(corsMapping, corsParameters);
            LOGGER.info("CORS support enabled on {}", corsMapping);
        }

        return InitState.INITIALIZED;
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        if (containerContext != null && ServletContext.class.isAssignableFrom(containerContext.getClass())) {
            this.servletContext = (ServletContext) containerContext;
        }
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(ConfigurationProvider.class);
    }

    @Override
    public Object nativeUnitModule() {
        return webCorsModule;
    }
}
