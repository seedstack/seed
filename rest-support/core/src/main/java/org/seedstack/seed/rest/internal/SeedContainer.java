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

import com.google.inject.Injector;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import com.sun.jersey.spi.container.servlet.WebConfig;

import javax.inject.Inject;
import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


class SeedContainer extends GuiceContainer {
    private static final long serialVersionUID = 1L;

    private final List<ResourceFilterFactory> resourceFilterFactories;

    @SuppressWarnings("all")
    @Inject
    SeedContainer(Injector injector, Set<ResourceFilterFactory> resourceFilterFactories) {
        super(injector);
        this.resourceFilterFactories = new ArrayList<ResourceFilterFactory>(resourceFilterFactories);
    }

    @SuppressWarnings("all")
    @Override
    protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props, WebConfig webConfig) throws ServletException {
        Map<String, Object> propertiesAndFeatures = new HashMap<String, Object>();

        // Set resource filters
        propertiesAndFeatures.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES, this.resourceFilterFactories);

        ResourceConfig resourceConfig = new DefaultResourceConfig();
        resourceConfig.setPropertiesAndFeatures(propertiesAndFeatures);
        return resourceConfig;
    }
}
