/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.internal;

import com.sun.xml.ws.transport.http.ResourceLoader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

class SeedResourceLoader implements ResourceLoader {
    private final Set<String> resourcePaths;
    private final ClassLoader classLoader;

    SeedResourceLoader(ClassLoader classLoader, Set<String> resourcePaths) {
        this.classLoader = classLoader;
        this.resourcePaths = resourcePaths;
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        return classLoader.getResource(path);
    }

    @Override
    public URL getCatalogFile() throws MalformedURLException {
        return classLoader.getResource(WSPlugin.WS_RESOURCES_LOCATION + "jax-ws-catalog.xml");
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        return resourcePaths;
    }
}
