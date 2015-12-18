/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.kametic.specifications.Specification;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;
import org.seedstack.seed.core.utils.SeedConfigurationUtils;
import org.seedstack.seed.rest.RelRegistry;
import org.seedstack.seed.rest.ResourceFiltering;
import org.seedstack.seed.rest.internal.exceptionmapper.AuthenticationExceptionMapper;
import org.seedstack.seed.rest.internal.exceptionmapper.AuthorizationExceptionMapper;
import org.seedstack.seed.rest.internal.exceptionmapper.InternalErrorExceptionMapper;
import org.seedstack.seed.rest.internal.exceptionmapper.WebApplicationExceptionMapper;
import org.seedstack.seed.rest.internal.hal.RelRegistryImpl;
import org.seedstack.seed.rest.internal.jsonhome.JsonHome;
import org.seedstack.seed.rest.internal.jsonhome.JsonHomeRootResource;
import org.seedstack.seed.rest.internal.jsonhome.Resource;
import org.seedstack.seed.rest.spi.RootResource;
import org.seedstack.seed.web.internal.WebPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * This plugin enables JAX-RS usage in SEED applications. The JAX-RS implementation is Jersey.
 *
 * @author adrien.lauer@mpsa.com
 */
public class Jersey1Plugin extends AbstractPlugin {

    private static final String REST_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.seed.rest";
    private static final Logger LOGGER = LoggerFactory.getLogger(Jersey1Plugin.class);

    private final Map<Variant, Class<? extends RootResource>> rootResourceClasses = new HashMap<Variant, Class<? extends RootResource>>();
    private final Specification<Class<?>> resourcesSpecification = new JaxRsResourceSpecification();
    private final Specification<Class<?>> providersSpecification = new JaxRsProviderSpecification();

    private WebPlugin webPlugin;
    private Configuration restConfiguration;
    private RelRegistry relRegistry;
    private JsonHome jsonHome;
    private String restPath;
    private String jspPath;
    private ServletContext servletContext;

    @Inject
    private Injector injector;

    @Override
    public String name() {
        return "seed-jersey1";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .annotationType(ResourceFiltering.class)
                .specification(providersSpecification)
                .specification(resourcesSpecification)
                .build();
    }

    @Override
    public InitState init(InitContext initContext) {
        collectPlugins(initContext);

        Map<Specification, Collection<Class<?>>> scannedClassesBySpecification = initContext.scannedTypesBySpecification();
        Collection<Class<?>> providerClasses = scannedClassesBySpecification.get(providersSpecification);
        Collection<Class<?>> resourceClasses = scannedClassesBySpecification.get(resourcesSpecification);

        restPath = restConfiguration.getString("path", "");
        jspPath = restConfiguration.getString("jsp-path", "/WEB-INF/jsp");

        // Scan resource for HAL and JSON-HOME
        scanResources(restPath, restConfiguration, resourceClasses);

        // Register JSON-HOME as root resource
        if (!restConfiguration.getBoolean("disable-json-home", false)) {
            registerRootResource(new Variant(new MediaType("application", "json"), null, null), JsonHomeRootResource.class);
        }

        // Skip the rest of the init phase if we are not in a servlet context
        if (servletContext == null) {
            LOGGER.info("No servlet context detected, REST support disabled");
            return InitState.INITIALIZED;
        }

        Set<Class<? extends ResourceFilterFactory>> resourceFilterFactories = scanResourceFilterFactories(initContext);

        Map<String, String> jerseyParameters = scanJerseyParameters();

        webPlugin.registerAdditionalModule(
                new Jersey1Module(
                        rootResourceClasses,
                        resourceClasses,
                        providerClasses,
                        jerseyParameters,
                        resourceFilterFactories,
                        restPath, jspPath, jsonHome)
        );

        return InitState.INITIALIZED;
    }

    private void collectPlugins(InitContext initContext) {
        restConfiguration = initContext.dependency(ConfigurationProvider.class).getConfiguration().subset(Jersey1Plugin.REST_PLUGIN_CONFIGURATION_PREFIX);
        webPlugin = initContext.dependency(WebPlugin.class);
    }

    @Override
    public void start(Context context) {
        if (servletContext != null) {
            SeedContainer seedContainer = injector.getInstance(SeedContainer.class);
            if (restConfiguration.getBoolean("map-security-exceptions", true)) {
                seedContainer.registerClass(AuthenticationExceptionMapper.class);
                seedContainer.registerClass(AuthorizationExceptionMapper.class);
            }

            if (restConfiguration.getBoolean("map-all-exceptions", true)) {
                seedContainer.registerClass(WebApplicationExceptionMapper.class);
                injector.injectMembers(InternalErrorExceptionMapper.class);
                seedContainer.registerClass(InternalErrorExceptionMapper.class);
            }
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
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(ConfigurationProvider.class, WebPlugin.class);
    }

    public void registerRootResource(Variant variant, Class<? extends RootResource> rootResource) {
        rootResourceClasses.put(variant, rootResource);
    }

    public String getRestPath() {
        return restPath;
    }

    public String getJspPath() {
        return jspPath;
    }
}
