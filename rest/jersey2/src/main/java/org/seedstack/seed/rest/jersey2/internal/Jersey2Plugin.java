/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;
import org.glassfish.jersey.servlet.ServletProperties;
import org.seedstack.seed.rest.RestConfig;
import org.seedstack.seed.rest.internal.RestPlugin;
import org.seedstack.seed.rest.spi.RestProvider;
import org.seedstack.seed.web.spi.FilterDefinition;
import org.seedstack.seed.web.spi.ListenerDefinition;
import org.seedstack.seed.web.spi.SeedFilterPriority;
import org.seedstack.seed.web.spi.ServletDefinition;
import org.seedstack.seed.web.spi.WebProvider;
import org.seedstack.shed.reflect.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        return Lists.newArrayList(RestPlugin.class, RestProvider.class);
    }

    @Override
    public InitState init(InitContext initContext) {
        RestPlugin restPlugin = initContext.dependency(RestPlugin.class);

        if (restPlugin.isEnabled()) {
            RestConfig restConfig = restPlugin.getRestConfig();

            Set<Class<?>> resources = new HashSet<>();
            Set<Class<?>> providers = new HashSet<>();
            List<RestProvider> restProviders = initContext.dependencies(RestProvider.class);
            for (RestProvider restProvider : restProviders) {
                resources.addAll(restProvider.resources());
                providers.addAll(filterClasses(restProvider.providers()));
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} JAX-RS resource(s) detected: {}", resources.size(), resources);
            } else {
                LOGGER.info("{} JAX-RS resource(s) detected (set logger to DEBUG to see them)", resources.size());
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} JAX-RS provider(s) detected: {}", providers.size(), providers);
            } else {
                LOGGER.info("{} JAX-RS provider(s) detected (set logger to DEBUG to see them)", providers.size());
            }

            Set<Class<?>> enabledFeatures = new HashSet<>();
            if (isMultipartFeaturePresent()) {
                enabledFeatures.add(MultiPartFeature.class);
                LOGGER.debug("Multipart feature is enabled");
            }

            if (isFreemarkerFeaturePresent()) {
                enabledFeatures.add(FreemarkerMvcFeature.class);
                LOGGER.debug("FreeMarker feature is enabled");
            }

            if (isJspFeaturePresent()) {
                enabledFeatures.add(JspMvcFeature.class);
                LOGGER.debug("JSP feature is enabled");
            }
            for (Class<?> featureClass : filterClasses(restConfig.getFeatures())) {
                enabledFeatures.add(featureClass);
                LOGGER.debug("Feature {} is enabled", featureClass.getCanonicalName());
            }

            Map<String, Object> jersey2Properties = buildJerseyProperties(restConfig);
            if (LOGGER.isTraceEnabled()) {
                for (Map.Entry<String, Object> entry : jersey2Properties.entrySet()) {
                    LOGGER.debug("Jersey property {} = {}", entry.getKey(), entry.getValue());
                }
            }

            jersey2filterDefinition = new FilterDefinition("jersey2", SeedServletContainer.class);
            jersey2filterDefinition.setPriority(SeedFilterPriority.JERSEY);
            jersey2filterDefinition.setAsyncSupported(true);
            jersey2filterDefinition.addMappings(new FilterDefinition.Mapping(restConfig.getPath() + "/*"));
            jersey2filterDefinition.addInitParameters(buildInitParams(jersey2Properties));

            seedServletContainer = new SeedServletContainer(resources, providers, enabledFeatures, jersey2Properties);

            LOGGER.info("Jersey 2 serving JAX-RS resources on {}/*", restConfig.getPath());
        }

        return InitState.INITIALIZED;
    }

    private Set<Class<?>> filterClasses(Collection<Class<?>> classes) {
        Set<Class<?>> result = new HashSet<>();

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

    private Map<String, Object> buildJerseyProperties(RestConfig restConfig) {
        Map<String, Object> jerseyProperties = new HashMap<>();

        // Default configuration values
        jerseyProperties.put(ServletProperties.FILTER_FORWARD_ON_404, true);
        jerseyProperties.put(ServerProperties.WADL_FEATURE_DISABLE, true);

        // User-defined configuration values
        jerseyProperties.putAll(restConfig.getJerseyProperties());

        // Forced configuration values
        jerseyProperties.put(ServletProperties.FILTER_CONTEXT_PATH, restConfig.getPath());
        if (isJspFeaturePresent()) {
            jerseyProperties.put(JspMvcFeature.TEMPLATE_BASE_PATH, restConfig.getJspPath());
        }

        return jerseyProperties;
    }

    private Map<String, String> buildInitParams(Map<String, Object> jerseyProperties) {
        Map<String, String> initParams = new HashMap<>();

        // Those properties must be defined as init parameters of the filter
        if (jerseyProperties.containsKey(ServletProperties.FILTER_CONTEXT_PATH)) {
            initParams.put(ServletProperties.FILTER_CONTEXT_PATH,
                    (String) jerseyProperties.get(ServletProperties.FILTER_CONTEXT_PATH));
        }
        if (jerseyProperties.containsKey(ServletProperties.FILTER_STATIC_CONTENT_REGEX)) {
            initParams.put(ServletProperties.FILTER_STATIC_CONTENT_REGEX,
                    (String) jerseyProperties.get(ServletProperties.FILTER_STATIC_CONTENT_REGEX));
        }

        return initParams;
    }

    private boolean isJspFeaturePresent() {
        return Classes.optional("org.glassfish.jersey.server.mvc.jsp.JspMvcFeature").isPresent();
    }

    private boolean isFreemarkerFeaturePresent() {
        return Classes.optional("org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature").isPresent();
    }

    private boolean isMultipartFeaturePresent() {
        return Classes.optional("org.glassfish.jersey.media.multipart.MultiPartFeature").isPresent();
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
