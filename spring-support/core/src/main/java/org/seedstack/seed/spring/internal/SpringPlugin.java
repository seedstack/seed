/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.spring.internal;

import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import io.nuun.kernel.spi.DependencyInjectionProvider;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This plugin provides Spring integration, converting any Spring bean defined in configured contexts in a named
 * Guice binding.
 *
 * @author adrien.lauer@mpsa.com
 * @author epo.jemba@ext.mpsa.com
 */
public class SpringPlugin extends AbstractPlugin {
    public static final String SPRING_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.seed.spring";
    public static final String APPLICATION_CONTEXT_REGEX = ".*-context.xml$";

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringPlugin.class);

    private final Set<String> applicationContextsPaths = new HashSet<String>();
    private ClassPathXmlApplicationContext globalApplicationContext;

    @Override
    public String name() {
        return "seed-spring-plugin";
    }

    @Override
    public String pluginPackageRoot() {
        return "META-INF.spring";
    }

    @Override
    public InitState init(InitContext initContext) {
        ApplicationPlugin applicationPlugin = (ApplicationPlugin) initContext.pluginsRequired().iterator().next();
        Configuration springConfiguration = applicationPlugin.getApplication().getConfiguration().subset(SPRING_PLUGIN_CONFIGURATION_PREFIX);

        Map<String, Collection<String>> scannedApplicationContexts = initContext.mapResourcesByRegex();

        SeedConfigurationFactoryBean.configuration = applicationPlugin.getApplication().getConfiguration();

        boolean autodetect = springConfiguration.getBoolean("autodetect", true);
        for (String applicationContextPath : scannedApplicationContexts.get(APPLICATION_CONTEXT_REGEX)) {
            if (autodetect && applicationContextPath.startsWith("META-INF/spring")) {
                applicationContextsPaths.add(applicationContextPath);
                LOGGER.info("Autodetected spring context at " + applicationContextPath);
            }
        }

        if (springConfiguration.containsKey("contexts")) {
            String[] explicitContexts = springConfiguration.getStringArray("contexts");
            for (String explicitContext : explicitContexts) {
                applicationContextsPaths.add(explicitContext);
                LOGGER.info("Configured spring context at " + explicitContext);
            }
        } else if (springConfiguration.containsKey("context")) {
            String explicitContext = springConfiguration.getString("context");
            applicationContextsPaths.add(explicitContext);
            LOGGER.info("Configured spring context at " + explicitContext);
        }

        LOGGER.info("Initializing spring contexts " + applicationContextsPaths);
        globalApplicationContext = new ClassPathXmlApplicationContext(this.applicationContextsPaths.toArray(new String[this.applicationContextsPaths.size()]));
        return InitState.INITIALIZED;
    }

    @Override
    public void stop() {
        if (globalApplicationContext != null) {
            LOGGER.info("Closing spring contexts " + applicationContextsPaths);
            globalApplicationContext.close();
        }
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        return plugins;
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().resourcesRegex(APPLICATION_CONTEXT_REGEX).build();
    }

    @Override
    public Object nativeUnitModule() {
        return globalApplicationContext;
    }

    @Override
    public DependencyInjectionProvider dependencyInjectionProvider() {
        return new SpringDependencyInjectionProvider();
    }
}
