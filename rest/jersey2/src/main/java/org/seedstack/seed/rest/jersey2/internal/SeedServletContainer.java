/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.internal;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.Map;
import java.util.Set;

class SeedServletContainer extends ServletContainer {
    SeedServletContainer(Set<Class<?>> resources, Set<Class<?>> providers, Set<Class<?>> features, Map<String, ?> properties) {
        super(new InternalResourceConfig(resources, providers, features, properties));
    }

    static private class InternalResourceConfig extends ResourceConfig {
        public InternalResourceConfig(Set<Class<?>> resources, Set<Class<?>> providers, Set<Class<?>> features, Map<String, ?> properties) {
            registerClasses(resources);
            registerClasses(providers);
            registerClasses(features);
            setProperties(properties);
        }
    }
}
