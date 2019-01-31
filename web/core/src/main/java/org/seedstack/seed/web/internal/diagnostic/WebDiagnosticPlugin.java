/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.diagnostic;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import java.util.List;
import javax.servlet.ServletContext;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.diagnostic.DiagnosticManager;
import org.seedstack.seed.web.WebConfig;
import org.seedstack.seed.web.spi.FilterDefinition;
import org.seedstack.seed.web.spi.ListenerDefinition;
import org.seedstack.seed.web.spi.SeedFilterPriority;
import org.seedstack.seed.web.spi.ServletDefinition;
import org.seedstack.seed.web.spi.WebProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebDiagnosticPlugin extends AbstractSeedPlugin implements WebProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebDiagnosticPlugin.class);
    private DiagnosticManager diagnosticManager;
    private ServletContext servletContext;
    private WebConfig webConfig;

    @Override
    public String name() {
        return "web-diagnostic";
    }

    @Override
    protected void setup(SeedRuntime seedRuntime) {
        diagnosticManager = seedRuntime.getDiagnosticManager();
        servletContext = seedRuntime.contextAs(ServletContext.class);
    }

    @Override
    public InitState initialize(InitContext initContext) {
        webConfig = getConfiguration(WebConfig.class);

        if (servletContext != null) {
            diagnosticManager.registerDiagnosticInfoCollector(
                    "web",
                    new WebDiagnosticCollector(servletContext)
            );
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        if (webConfig.isRequestDiagnosticEnabled()) {
            return new WebDiagnosticModule();
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
        if (webConfig.isRequestDiagnosticEnabled()) {
            LOGGER.info("Per-request diagnostic enabled, a diagnostic file will be dumped for each request exception");

            FilterDefinition filterDefinition = new FilterDefinition("web-diagnostic", WebDiagnosticFilter.class);
            filterDefinition.setPriority(SeedFilterPriority.DIAGNOSTIC);
            filterDefinition.setAsyncSupported(true);
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
