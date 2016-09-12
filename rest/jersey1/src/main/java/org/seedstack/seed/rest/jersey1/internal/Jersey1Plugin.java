/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey1.internal;

import com.google.common.collect.Lists;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.seedstack.seed.rest.ResourceFiltering;
import org.seedstack.seed.rest.internal.RestConfig;
import org.seedstack.seed.rest.internal.RestPlugin;
import org.seedstack.seed.web.spi.FilterDefinition;
import org.seedstack.seed.web.spi.ListenerDefinition;
import org.seedstack.seed.web.spi.ServletDefinition;
import org.seedstack.seed.web.spi.WebProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This plugin provides JAX-RS integration with Jersey 1.
 *
 * @author adrien.lauer@mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 */
public class Jersey1Plugin extends AbstractPlugin implements WebProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(Jersey1Plugin.class);

    private FilterDefinition jersey1FilterDefinition;
    private Jersey1Module jersey1Module;

    @Override
    public String name() {
        return "jersey1";
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(RestPlugin.class);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().annotationType(ResourceFiltering.class).build();
    }

    @Override
    public InitState init(InitContext initContext) {
        RestPlugin restPlugin = initContext.dependency(RestPlugin.class);

        if (restPlugin.isEnabled()) {
            RestConfig restConfig = restPlugin.getRestConfig();

            jersey1Module = new Jersey1Module(scanResourceFilterFactories(initContext));

            jersey1FilterDefinition = new FilterDefinition("jersey1", SeedContainer.class);
            jersey1FilterDefinition.addMappings(new FilterDefinition.Mapping(restConfig.getPath() + "/*"));
            jersey1FilterDefinition.addInitParameters(buildInitParams(restConfig));

            LOGGER.info("Jersey 1 serving JAX-RS resources on {}/*", restConfig.getPath());
        }

        return InitState.INITIALIZED;
    }

    private Set<Class<? extends ResourceFilterFactory>> scanResourceFilterFactories(InitContext initContext) {
        Map<Class<? extends Annotation>, Collection<Class<?>>> scannedClassesByAnnotationClass = initContext.scannedClassesByAnnotationClass();
        Set<Class<? extends ResourceFilterFactory>> resourceFilterFactories = new HashSet<>();
        Collection<Class<?>> resourceFilterFactoryClasses = scannedClassesByAnnotationClass.get(ResourceFiltering.class);

        if (resourceFilterFactoryClasses != null) {
            for (Class<?> candidate : resourceFilterFactoryClasses) {
                if (ResourceFilterFactory.class.isAssignableFrom(candidate)) {
                    resourceFilterFactories.add(candidate.asSubclass(ResourceFilterFactory.class));
                }
            }
        }

        return resourceFilterFactories;
    }

    private Map<String, String> buildInitParams(RestConfig restConfig) {
        Map<String, String> initParams = new HashMap<>();

        // Default configuration values
        initParams.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
        initParams.put("com.sun.jersey.config.feature.FilterForwardOn404", "true");
        initParams.put("com.sun.jersey.config.feature.DisableWADL", "true");

        // User configuration values
        initParams.putAll(restConfig.getJerseyProperties());

        // Forced configuration values
        initParams.put("com.sun.jersey.config.property.JSPTemplatesBasePath", restConfig.getJspPath());
        initParams.put("com.sun.jersey.config.feature.FilterContextPath", restConfig.getPath());

        return initParams;
    }

    @Override
    public Object nativeUnitModule() {
        return jersey1Module;
    }

    @Override
    public List<ServletDefinition> servlets() {
        return null;
    }

    @Override
    public List<FilterDefinition> filters() {
        return jersey1FilterDefinition != null ? Lists.newArrayList(jersey1FilterDefinition) : null;
    }

    @Override
    public List<ListenerDefinition> listeners() {
        return null;
    }
}
