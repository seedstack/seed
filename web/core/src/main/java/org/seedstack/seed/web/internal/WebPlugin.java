/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.reflections.util.ClasspathHelper;
import org.seedstack.seed.SeedRuntime;
import org.seedstack.seed.core.internal.CorePlugin;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This plugin provides Web support for applications and Web-specific diagnostics.
 *
 * @author adrien.lauer@mpsa.com
 */
public class WebPlugin extends AbstractPlugin {
    public static final String WEB_PLUGIN_PREFIX = "org.seedstack.seed.web";
    private static final Logger LOGGER = LoggerFactory.getLogger(WebPlugin.class);

    private ServletContext servletContext;
    private WebModule webModule;

    @Override
    public String name() {
        return "web";
    }

    @Override
    public Set<URL> computeAdditionalClasspathScan() {
        Set<URL> additionalUrls = new HashSet<URL>();

        // resource paths for WEB-INF/lib can be null when SEED run in the Undertow servlet container
        if (servletContext != null && servletContext.getResourcePaths("/WEB-INF/lib") != null) {
            additionalUrls.addAll(ClasspathHelper.forWebInfLib(servletContext));
            additionalUrls.add(ClasspathHelper.forWebInfClasses(servletContext));
        }

        LOGGER.debug("{} URL(s) were determined from Web classpath", additionalUrls.size());

        return additionalUrls;
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(CorePlugin.class, ConfigurationProvider.class);
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        servletContext = ((SeedRuntime)containerContext).contextAs(ServletContext.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState init(InitContext initContext) {
        if (servletContext == null) {
            LOGGER.info("No servlet context detected, web support disabled");
            return InitState.INITIALIZED;
        }

        WebDiagnosticCollector webDiagnosticCollector = new WebDiagnosticCollector(servletContext);
        initContext.dependency(CorePlugin.class).registerDiagnosticCollector(WEB_PLUGIN_PREFIX, webDiagnosticCollector);

        Configuration webConfiguration = initContext.dependency(ConfigurationProvider.class).getConfiguration().subset(WebPlugin.WEB_PLUGIN_PREFIX);

        boolean requestDiagnosticEnabled = webConfiguration.getBoolean("request-diagnostic.enabled", false);
        if (requestDiagnosticEnabled) {
            LOGGER.info("Per-request diagnostic enabled");
        }

        webModule = new WebModule(requestDiagnosticEnabled);

        return InitState.INITIALIZED;
    }

    @Override
    public Module nativeUnitModule() {
        return webModule;
    }
}
