/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.cors;

import com.google.common.collect.Lists;
import com.thetransactioncompany.cors.CORSFilter;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;
import org.seedstack.seed.core.utils.SeedConfigurationUtils;
import org.seedstack.seed.web.FilterDefinition;
import org.seedstack.seed.web.ListenerDefinition;
import org.seedstack.seed.web.ServletDefinition;
import org.seedstack.seed.web.WebProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.seedstack.seed.web.internal.WebPlugin.WEB_PLUGIN_PREFIX;

public class WebCORSPlugin extends AbstractPlugin implements WebProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebCORSPlugin.class);

    private String corsMapping;
    private Map<String, String> corsParameters;

    @Override
    public String name() {
        return "web-cors";
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(ConfigurationProvider.class);
    }

    @Override
    public InitState init(InitContext initContext) {
        Configuration webConfiguration = initContext.dependency(ConfigurationProvider.class).getConfiguration().subset(WEB_PLUGIN_PREFIX);

        if (webConfiguration.getBoolean("cors.enabled", false)) {
            corsMapping = webConfiguration.getString("cors.url-mapping", "/*");
            corsParameters = new HashMap<String, String>();
            Properties corsProperties = SeedConfigurationUtils.buildPropertiesFromConfiguration(webConfiguration, "cors.property");
            for (Object key : corsProperties.keySet()) {
                corsParameters.put("cors." + key.toString(), corsProperties.getProperty(key.toString()));
            }
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        if (corsMapping != null) {
            return new WebCORSModule();
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
        if (corsMapping != null) {
            LOGGER.info("CORS support enabled on {}", corsMapping);

            FilterDefinition filterDefinition = new FilterDefinition("web-cors", CORSFilter.class);
            filterDefinition.setPriority(1000);
            filterDefinition.setAsyncSupported(true);
            filterDefinition.addInitParameters(corsParameters);
            filterDefinition.addMappings(new FilterDefinition.Mapping("/*"));
            return Lists.newArrayList(filterDefinition);
        } else {
            return null;
        }
    }

    @Override
    public List<ListenerDefinition> listeners() {
        return null;
    }
}
