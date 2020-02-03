/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.web.WebConfig;
import org.seedstack.seed.web.spi.FilterDefinition;
import org.seedstack.seed.web.spi.ListenerDefinition;
import org.seedstack.seed.web.spi.SeedFilterPriority;
import org.seedstack.seed.web.spi.ServletDefinition;
import org.seedstack.seed.web.spi.WebProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebCORSPlugin extends AbstractSeedPlugin implements WebProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebCORSPlugin.class);

    private WebConfig.CORSConfig corsConfig;

    @Override
    public String name() {
        return "web-cors";
    }

    @Override
    protected InitState initialize(InitContext initContext) {
        corsConfig = getConfiguration(WebConfig.CORSConfig.class);
        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        if (corsConfig.isEnabled()) {
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
        if (corsConfig.isEnabled()) {
            LOGGER.info("CORS support enabled on {}", corsConfig.getPath());

            FilterDefinition filterDefinition = new FilterDefinition("web-cors", CORSFilter.class);
            filterDefinition.setPriority(SeedFilterPriority.CORS);
            filterDefinition.setAsyncSupported(true);
            filterDefinition.addInitParameters(buildCorsParameters());
            filterDefinition.addMappings(new FilterDefinition.Mapping(corsConfig.getPath()));
            return Lists.newArrayList(filterDefinition);
        } else {
            return null;
        }
    }

    private Map<String, String> buildCorsParameters() {
        Map<String, String> corsParameters = new HashMap<>();
        for (Map.Entry<String, String> entry : corsConfig.getProperties().entrySet()) {
            corsParameters.put("cors." + entry.getKey(), entry.getValue());
        }
        return corsParameters;
    }

    @Override
    public List<ListenerDefinition> listeners() {
        return null;
    }
}
