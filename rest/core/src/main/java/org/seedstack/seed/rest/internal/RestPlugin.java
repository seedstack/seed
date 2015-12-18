/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.kametic.specifications.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class RestPlugin extends AbstractPlugin {

    public static final Specification<Class<?>> resourcesSpecification = new JaxRsResourceSpecification();
    public static final Specification<Class<?>> providersSpecification = new JaxRsProviderSpecification();

    private Collection<Class<?>> resources = new ArrayList<Class<?>>();
    private Collection<Class<?>> providers = new ArrayList<Class<?>>();

    @Override
    public String name() {
        return "seed-rest";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .specification(providersSpecification)
                .specification(resourcesSpecification)
                .build();
    }

    @Override
    public InitState init(InitContext initContext) {
        Map<Specification, Collection<Class<?>>> scannedClasses = initContext.scannedTypesBySpecification();
        resources = scannedClasses.get(RestPlugin.resourcesSpecification);
        providers = scannedClasses.get(RestPlugin.providersSpecification);
        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new RestModule(resources, providers);
    }
}
