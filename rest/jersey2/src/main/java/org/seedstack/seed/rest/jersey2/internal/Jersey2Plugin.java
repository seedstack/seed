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
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.rest.internal.RestConfiguration;
import org.seedstack.seed.rest.internal.RestPlugin;
import org.seedstack.seed.rest.spi.RestProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Jersey2Plugin extends AbstractPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(Jersey2Plugin.class);
    private Jersey2Module jersey2Module;

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

            List<RestProvider> restProviders = initContext.dependencies(RestProvider.class);
            Set<Class<?>> resources = new HashSet<Class<?>>();
            Set<Class<?>> providers = new HashSet<Class<?>>();
            for (RestProvider restProvider : restProviders) {
                resources.addAll(restProvider.resources());
                providers.addAll(filterClasses(restProvider.providers()));
            }

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

            jersey2Module = new Jersey2Module(restConfiguration, resources, providers, enabledFeatures);
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return jersey2Module;
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

    static boolean isJspFeaturePresent() {
        return SeedReflectionUtils.forName("org.glassfish.jersey.server.mvc.jsp.JspMvcFeature").isPresent();
    }

    static boolean isMultipartFeaturePresent() {
        return SeedReflectionUtils.forName("org.glassfish.jersey.media.multipart.MultiPartFeature").isPresent();
    }
}
