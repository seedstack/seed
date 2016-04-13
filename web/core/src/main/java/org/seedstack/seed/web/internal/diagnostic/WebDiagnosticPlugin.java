/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.diagnostic;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.seedstack.seed.SeedRuntime;
import org.seedstack.seed.core.internal.CorePlugin;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;
import org.seedstack.seed.web.FilterDefinition;
import org.seedstack.seed.web.ListenerDefinition;
import org.seedstack.seed.web.ServletDefinition;
import org.seedstack.seed.web.WebProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.util.Collection;
import java.util.List;

import static org.seedstack.seed.web.internal.WebPlugin.WEB_PLUGIN_PREFIX;

public class WebDiagnosticPlugin extends AbstractPlugin implements WebProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebDiagnosticPlugin.class);

    private ServletContext servletContext;
    private boolean perRequestDiagnosticEnabled;

    @Override
    public String name() {
        return "web-diagnostic";
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        servletContext = ((SeedRuntime) containerContext).contextAs(ServletContext.class);
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(CorePlugin.class, ConfigurationProvider.class);
    }

    @Override
    public InitState init(InitContext initContext) {
        perRequestDiagnosticEnabled = initContext
                .dependency(ConfigurationProvider.class)
                .getConfiguration()
                .subset(WEB_PLUGIN_PREFIX)
                .getBoolean("request-diagnostic.enabled", false);

        if (servletContext != null) {
            initContext.dependency(CorePlugin.class).registerDiagnosticCollector(
                    WEB_PLUGIN_PREFIX,
                    new WebDiagnosticCollector(servletContext)
            );
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        if (perRequestDiagnosticEnabled) {
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
        if (perRequestDiagnosticEnabled) {
            LOGGER.info("Per-request diagnostic enabled, a diagnostic file will be dumped for each request exception");

            FilterDefinition filterDefinition = new FilterDefinition("web-diagnostic", WebDiagnosticFilter.class);
            filterDefinition.setPriority(10000);
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
