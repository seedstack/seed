/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.internal;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;
import org.glassfish.jersey.servlet.ServletProperties;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.rest.internal.RestConfiguration;
import org.seedstack.seed.rest.internal.RestPlugin;
import org.seedstack.seed.rest.spi.RestProvider;
import org.seedstack.seed.web.FilterDefinition;
import org.seedstack.seed.web.ListenerDefinition;
import org.seedstack.seed.web.ServletDefinition;
import org.seedstack.seed.web.WebProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Jersey2Plugin extends AbstractPlugin implements WebProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(Jersey2Plugin.class);

    private FilterDefinition jersey2filterDefinition;
    private SeedServletContainer seedServletContainer;

    @Override
    public String name() {
        return "jersey2";
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(RestPlugin.class, RestProvider.class);
    }

    @Override
    public InitState init(InitContext initContext) {
        RestPlugin restPlugin = initContext.dependency(RestPlugin.class);

        if (restPlugin.isEnabled()) {
            RestConfiguration restConfiguration = restPlugin.getConfiguration();

            Set<Class<?>> resources = new HashSet<Class<?>>();
            Set<Class<?>> providers = new HashSet<Class<?>>();
            List<RestProvider> restProviders = initContext.dependencies(RestProvider.class);
            for (RestProvider restProvider : restProviders) {
                resources.addAll(restProvider.resources());
                providers.addAll(filterClasses(restProvider.providers()));
            }
            LOGGER.debug("Detected {} JAX-RS resource(s)", resources.size());
            LOGGER.debug("Detected {} JAX-RS provider(s)", providers.size());

            Set<Class<?>> enabledFeatures = new HashSet<Class<?>>();
            if (isMultipartFeaturePresent()) {
                enabledFeatures.add(MultiPartFeature.class);
                LOGGER.trace("Detected and enabled JAX-RS multipart feature");
            }
            if (isJspFeaturePresent()) {
                enabledFeatures.add(JspMvcFeature.class);
                LOGGER.trace("Detected and enabled JAX-RS JSP feature");
            }
            for (Class<?> featureClass : filterClasses(restConfiguration.getFeatures())) {
                enabledFeatures.add(featureClass);
                LOGGER.trace("Enabled JAX-RS feature " + featureClass.getCanonicalName());
            }
            LOGGER.debug("Enabled {} JAX-RS feature(s)", enabledFeatures.size());

            Map<String, Object> jersey2Properties = buildJerseyProperties(restConfiguration);

            jersey2filterDefinition = new FilterDefinition("jersey2", SeedServletContainer.class);
            jersey2filterDefinition.setAsyncSupported(true);
            jersey2filterDefinition.addMappings(new FilterDefinition.Mapping(restConfiguration.getRestPath() + "/*"));
            jersey2filterDefinition.addInitParameters(buildInitParams(jersey2Properties));

            seedServletContainer = new SeedServletContainer(resources, providers, enabledFeatures, jersey2Properties);

            LOGGER.info("Jersey 2 serving JAX-RS resources on " + restConfiguration.getRestPath());
        }

        return InitState.INITIALIZED;
    }

    private Set<Class<?>> filterClasses(Collection<Class<?>> classes) {
        Set<Class<?>> result = new HashSet<Class<?>>();

        if (classes != null) {
            for (Class<?> aClass : classes) {
                ConstrainedTo annotation = aClass.getAnnotation(ConstrainedTo.class);
                if (annotation == null || annotation.value() == RuntimeType.SERVER) {
                    result.add(aClass);
                }
            }
        }

        return result;
    }

    private Map<String, Object> buildJerseyProperties(RestConfiguration restConfiguration) {
        Map<String, Object> jerseyProperties = new HashMap<String, Object>();

        // Default configuration values
        jerseyProperties.put(ServletProperties.FILTER_FORWARD_ON_404, true);
        jerseyProperties.put(ServerProperties.WADL_FEATURE_DISABLE, true);

        // User-defined configuration values
        jerseyProperties.putAll(propertiesToMap(restConfiguration.getJerseyProperties()));

        // Forced configuration values
        jerseyProperties.put(ServletProperties.FILTER_CONTEXT_PATH, restConfiguration.getRestPath());
        if (isJspFeaturePresent()) {
            jerseyProperties.put(JspMvcFeature.TEMPLATE_BASE_PATH, restConfiguration.getJspPath());
        }

        return jerseyProperties;
    }

    private Map<String, String> propertiesToMap(Properties properties) {
        Map<String, String> map = new HashMap<String, String>();

        for (Object key : properties.keySet()) {
            map.put(key.toString(), properties.getProperty(key.toString()));
        }

        return map;
    }

    private Map<String, String> buildInitParams(Map<String, Object> jerseyProperties) {
        Map<String, String> initParams = new HashMap<String, String>();

        // Those properties must be defined as init parameters of the filter
        if (jerseyProperties.containsKey(ServletProperties.FILTER_CONTEXT_PATH)) {
            initParams.put(ServletProperties.FILTER_CONTEXT_PATH, (String) jerseyProperties.get(ServletProperties.FILTER_CONTEXT_PATH));
        }
        if (jerseyProperties.containsKey(ServletProperties.FILTER_STATIC_CONTENT_REGEX)) {
            initParams.put(ServletProperties.FILTER_STATIC_CONTENT_REGEX, (String) jerseyProperties.get(ServletProperties.FILTER_STATIC_CONTENT_REGEX));
        }

        return initParams;
    }

    private boolean isJspFeaturePresent() {
        return SeedReflectionUtils.forName("org.glassfish.jersey.server.mvc.jsp.JspMvcFeature").isPresent();
    }

    private boolean isMultipartFeaturePresent() {
        return SeedReflectionUtils.forName("org.glassfish.jersey.media.multipart.MultiPartFeature").isPresent();
    }

    @Override
    public Object nativeUnitModule() {
        return seedServletContainer != null ? new Jersey2Module(seedServletContainer) : null;
    }

    @Override
    public List<ServletDefinition> servlets() {
        return null;
    }

    @Override
    public List<FilterDefinition> filters() {
        return jersey2filterDefinition != null ? Lists.newArrayList(jersey2filterDefinition) : null;
    }

    @Override
    public List<ListenerDefinition> listeners() {
        return null;
    }
}
