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
import com.google.inject.Injector;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.kametic.specifications.Specification;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.core.utils.SeedConfigurationUtils;
import org.seedstack.seed.rest.api.RelRegistry;
import org.seedstack.seed.rest.api.ResourceFiltering;
import org.seedstack.seed.rest.internal.exceptionmapper.AuthenticationExceptionMapper;
import org.seedstack.seed.rest.internal.exceptionmapper.AuthorizationExceptionMapper;
import org.seedstack.seed.rest.internal.exceptionmapper.InternalErrorExceptionMapper;
import org.seedstack.seed.rest.internal.exceptionmapper.WebApplicationExcetionMapper;
import org.seedstack.seed.rest.internal.hal.RelRegistryImpl;
import org.seedstack.seed.rest.internal.jsonhome.JsonHome;
import org.seedstack.seed.rest.internal.jsonhome.Resource;
import org.seedstack.seed.web.internal.WebPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.lang.annotation.Annotation;
import java.util.*;

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

    private WebPlugin webPlugin;
    private Configuration restConfiguration;

    private RelRegistry relRegistry;
    private JsonHome jsonHome;

    private ServletContext servletContext;

    @Inject
    private Injector injector;

    @Override
    public String name() {
        return "seed-rest-plugin";
    }

    @Override
    public InitState init(InitContext initContext) {
        // Initialize required and dependent plugins
        collectPlugins(initContext);

        Map<Specification, Collection<Class<?>>> scannedClassesBySpecification = initContext.scannedTypesBySpecification();
        Collection<Class<?>> resourceClasses = scannedClassesBySpecification.get(resourcesSpecification);

        String restPath = restConfiguration.getString("path", "");
        String jspPath = restConfiguration.getString("jsp-path", "/WEB-INF/jsp");

        // Scan resource for HAL and JSON-HOME
        scanResources(restPath, restConfiguration, resourceClasses);

        // Skip the rest of the init phase if we are not in a servlet context
        if (servletContext == null) {
            LOGGER.info("No servlet context detected, REST support disabled");
            return InitState.INITIALIZED;
        }

        Set<Class<? extends ResourceFilterFactory>> resourceFilterFactories = scanResourceFilterFactories(initContext);

        Map<String, String> jerseyParameters = scanJerseyParameters();

        webPlugin.registerAdditionalModule(
                new RestModule(
                        resourceClasses,
                        scannedClassesBySpecification.get(providersSpecification),
                        jerseyParameters,
                        resourceFilterFactories,
                        restPath, jspPath, jsonHome)
        );

        return InitState.INITIALIZED;
    }

    @Override
    public void start(Context context) {
        if (servletContext != null) {
            SeedContainer seedContainer = injector.getInstance(SeedContainer.class);
            seedContainer.registerClass(AuthenticationExceptionMapper.class);
            seedContainer.registerClass(AuthorizationExceptionMapper.class);

            if (restConfiguration.getBoolean("map-all-exceptions", true)) {
                seedContainer.registerClass(WebApplicationExcetionMapper.class);
                seedContainer.registerClass(InternalErrorExceptionMapper.class);
            }
        }
    }

    private void collectPlugins(InitContext initContext) {
        restConfiguration = null;
        webPlugin = null;
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
    }

    private Collection<Class<?>> scanResources(String restPath, Configuration restConfiguration, Collection<Class<?>> resourceClasses) {
        String baseRel = restConfiguration.getString("baseRel", "");
        String baseParam = restConfiguration.getString("baseParam", "");

        ResourceScanner resourceScanner = new ResourceScanner(restPath, baseRel, baseParam)
                .scan(resourceClasses);
        Map<String, Resource> resourceMap = resourceScanner.jsonHomeResources();

        relRegistry = new RelRegistryImpl(resourceScanner.halLinks());
        jsonHome = new JsonHome(resourceMap);

        return resourceClasses;
    }

    private Set<Class<? extends ResourceFilterFactory>> scanResourceFilterFactories(InitContext initContext) {
        Map<Class<? extends Annotation>, Collection<Class<?>>> scannedClassesByAnnotationClass = initContext.scannedClassesByAnnotationClass();

        Collection<Class<?>> resourceFilterFactoryClasses = scannedClassesByAnnotationClass.get(ResourceFiltering.class);

        Set<Class<? extends ResourceFilterFactory>> resourceFilterFactories = new HashSet<Class<? extends ResourceFilterFactory>>();

        if (resourceFilterFactoryClasses != null) {
            for (Class<?> candidate : resourceFilterFactoryClasses) {
                if (ResourceFilterFactory.class.isAssignableFrom(candidate)) {
                    resourceFilterFactories.add(candidate.asSubclass(ResourceFilterFactory.class));
                }
            }
        }
        return resourceFilterFactories;
    }

    private Map<String, String> scanJerseyParameters() {
        Map<String, String> jerseyParameters = new HashMap<String, String>();

        Properties jerseyProperties = SeedConfigurationUtils.buildPropertiesFromConfiguration(restConfiguration, "jersey.property");

        for (Object key : jerseyProperties.keySet()) {
            jerseyParameters.put(key.toString(), jerseyProperties.getProperty(key.toString()));
        }
        return jerseyParameters;
    }

    @Override
    public Object nativeUnitModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(RelRegistry.class).toInstance(relRegistry);
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
