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

import org.kametic.specifications.AbstractSpecification;
import org.seedstack.seed.core.utils.SeedConfigurationUtils;
import org.seedstack.seed.rest.api.Activity;
import org.seedstack.seed.rest.api.ResourceFiltering;
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
import org.seedstack.seed.web.internal.WebPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This plugin enables JAX-RS usage in SEED applications. The JAX-RS implementation is Jersey.
 *
 * @author adrien.lauer@mpsa.com
 */
public class RestPlugin extends AbstractPlugin {
    private static final String REST_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.seed.rest";
    private static final Logger LOGGER = LoggerFactory.getLogger(RestPlugin.class);

    private final Specification<Class<?>> resourcesSpecification = and(or(classAnnotatedWith(Path.class), classMethodsAnnotatedWith(Path.class)), not(classIsAbstract()));
    private final Specification<Class<?>> providersSpecification = or(and(classAnnotatedWith(Provider.class), classImplements(MessageBodyWriter.class)), and(classAnnotatedWith(Provider.class), classImplements(ContextResolver.class)), and(classAnnotatedWith(Provider.class), classImplements(MessageBodyReader.class)), and(classAnnotatedWith(Provider.class), classImplements(ExceptionMapper.class)));

    private ServletContext servletContext;

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


        webPlugin.registerAdditionalModule(
                new RestModule(
                        scannedClassesBySpecification.get(resourcesSpecification),
                        scannedClassesBySpecification.get(providersSpecification),
                        scannedClassesByAnnotationClass.get(Activity.class),
                        resourceFilterFactories,
                        jerseyParameters,
                        restPath,
                        jspPath)
        );

        return InitState.INITIALIZED;
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
        return classpathScanRequestBuilder().annotationType(Activity.class).annotationType(ResourceFiltering.class).specification(resourcesSpecification).specification(providersSpecification).build();
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        plugins.add(WebPlugin.class);
        return plugins;
    }

    /**
     * Checks if the class is abstract.
     * @return the specification
     */
    private Specification<Class<?>> classIsAbstract() {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {
                return candidate != null && Modifier.isAbstract(candidate.getModifiers());
            }
        };
    }
}
