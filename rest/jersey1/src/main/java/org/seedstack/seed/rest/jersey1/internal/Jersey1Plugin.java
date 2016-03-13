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
import org.seedstack.seed.rest.internal.RestPlugin;
import org.seedstack.seed.web.internal.WebPlugin;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This plugin provides JAX-RS integration with Jersey 1.
 *
 * @author adrien.lauer@mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 */
public class Jersey1Plugin extends AbstractPlugin {
    private Jersey1Module jersey1Module;

    @Override
    public String name() {
        return "jersey1";
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(WebPlugin.class, RestPlugin.class);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().annotationType(ResourceFiltering.class).build();
    }

    @Override
    public InitState init(InitContext initContext) {
        RestPlugin restPlugin = initContext.dependency(RestPlugin.class);

        if (restPlugin.isEnabled()) {
            jersey1Module = new Jersey1Module(
                    restPlugin.getConfiguration(),
                    scanResourceFilterFactories(initContext)
            );
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return jersey1Module;
    }

    private Set<Class<? extends ResourceFilterFactory>> scanResourceFilterFactories(InitContext initContext) {
        Map<Class<? extends Annotation>, Collection<Class<?>>> scannedClassesByAnnotationClass = initContext.scannedClassesByAnnotationClass();
        Set<Class<? extends ResourceFilterFactory>> resourceFilterFactories = new HashSet<Class<? extends ResourceFilterFactory>>();
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
}
