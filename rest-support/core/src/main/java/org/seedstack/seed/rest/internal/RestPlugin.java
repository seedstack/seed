/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import com.google.inject.AbstractModule;
import org.seedstack.seed.core.utils.SeedConfigurationUtils;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.kametic.specifications.Specification;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.rest.api.ResourceFiltering;
import org.seedstack.seed.rest.internal.jsonhome.JsonHome;
import org.seedstack.seed.web.internal.WebPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.HashMap;
import java.util.Properties;

/**
 * This plugin enables JAX-RS usage in SEED applications. The JAX-RS implementation is Jersey.
 *
 * @author adrien.lauer@mpsa.com
 */
public class RestPlugin extends AbstractPlugin {
    private static final String REST_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.seed.rest";
    private static final Logger LOGGER = LoggerFactory.getLogger(RestPlugin.class);

    private final Specification<Class<?>> resourcesSpecification = new JaxRsResourceSpecification();
    private final Specification<Class<?>> providersSpecification = new JaxRsProviderSpecification();

    private ServletContext servletContext;
    private JsonHome jsonHome;

    @Override
    public String name() {
        return "seed-rest-plugin";
    }

    @Override
    public InitState init(InitContext initContext) {
        if (servletContext == null) {
            LOGGER.info("No servlet context detected, REST support disabled");
            return InitState.INITIALIZED;
        }

        Configuration restConfiguration = null;
        WebPlugin webPlugin = null;
        for (Plugin plugin : initContext.pluginsRequired()) {
            if (plugin instanceof ApplicationPlugin) {
                restConfiguration = ((ApplicationPlugin) plugin).getApplication().getConfiguration().subset(RestPlugin.REST_PLUGIN_CONFIGURATION_PREFIX);
            } else if (plugin instanceof WebPlugin) {
                webPlugin = (WebPlugin) plugin;
            }
        }

        if (restConfiguration == null) {
            throw new PluginException("Unable to find SEED application plugin");
        }
        if (webPlugin == null) {
            throw new PluginException("Unable to find SEED Web plugin");
        }

        String restPath = restConfiguration.getString("path", "/rest");
        String jspPath = restConfiguration.getString("jsp-path", "/WEB-INF/jsp");

        Map<Class<? extends Annotation>, Collection<Class<?>>> scannedClassesByAnnotationClass = initContext.scannedClassesByAnnotationClass();
        Map<Specification, Collection<Class<?>>> scannedClassesBySpecification = initContext.scannedTypesBySpecification();

        Collection<Class<?>> resourceFilterFactoryClasses = scannedClassesByAnnotationClass.get(ResourceFiltering.class);
        Set<Class<? extends ResourceFilterFactory>> resourceFilterFactories = new HashSet<Class<? extends ResourceFilterFactory>>();
        if (resourceFilterFactoryClasses != null) {
            for (Class<?> candidate : resourceFilterFactoryClasses) {
                if (ResourceFilterFactory.class.isAssignableFrom(candidate)) {
                    resourceFilterFactories.add(candidate.asSubclass(ResourceFilterFactory.class));
                }
            }
        }

        Map<String, String> jerseyParameters = new HashMap<String, String>();
        Properties jerseyProperties = SeedConfigurationUtils.buildPropertiesFromConfiguration(restConfiguration, "jersey.property");
        for (Object key : jerseyProperties.keySet()) {
            jerseyParameters.put(key.toString(), jerseyProperties.getProperty(key.toString()));
        }

        String baseRel = restConfiguration.getString("baseRel", "");
//        if (baseRel == null) { //TODO SeedException
//            throw new IllegalArgumentException("Missing org.seedstack.seed.rest.baseRel property");
//        }

        jsonHome = new JsonHome(baseRel, scannedClassesBySpecification.get(resourcesSpecification));

        webPlugin.registerAdditionalModule(
                new RestModule(
                        scannedClassesBySpecification.get(resourcesSpecification),
                        scannedClassesBySpecification.get(providersSpecification),
                        jerseyParameters,
                        resourceFilterFactories,
                        restPath,
                        jspPath)
        );

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(JsonHome.class).toInstance(jsonHome);
            }
        };
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        if (containerContext != null && ServletContext.class.isAssignableFrom(containerContext.getClass())) {
            this.servletContext = (ServletContext) containerContext;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .annotationType(ResourceFiltering.class)
                .specification(providersSpecification)
                .specification(resourcesSpecification)
                .build();
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        plugins.add(WebPlugin.class);
        return plugins;
    }
}
