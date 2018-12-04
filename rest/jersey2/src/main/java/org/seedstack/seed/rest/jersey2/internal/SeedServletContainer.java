/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.jersey2.internal;

import java.util.Map;
import java.util.Set;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

class SeedServletContainer extends ServletContainer {
    private static final long serialVersionUID = 1L;

    SeedServletContainer(Set<Class<?>> resources, Set<Class<?>> providers, Set<Class<?>> features,
            Map<String, ?> properties) {
        super(new InternalResourceConfig(resources, providers, features, properties));
    }

    static private class InternalResourceConfig extends ResourceConfig {
        InternalResourceConfig(Set<Class<?>> resources, Set<Class<?>> providers, Set<Class<?>> features,
                Map<String, ?> properties) {
            registerClasses(resources);
            registerClasses(providers);
            registerClasses(features);
            setProperties(properties);
        }
    }
}
