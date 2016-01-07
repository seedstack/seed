/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.internal;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

class SeedServletContainer extends ServletContainer {
    SeedServletContainer(Collection<Class<?>> resources, Collection<Class<?>> providers, Map<String, ?> properties) {
        super(new InternalResourceConfig(resources, providers, properties));
    }

    static private class InternalResourceConfig extends ResourceConfig {
        public InternalResourceConfig(Collection<Class<?>> resources, Collection<Class<?>> providers, Map<String, ?> properties) {
            registerClasses(new HashSet<Class<?>>(resources));
            registerClasses(new HashSet<Class<?>>(providers));
            setProperties(properties);
        }
    }
}
