/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal;

import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.spi.ConfigurationProvider;
import org.seedstack.seed.rest.RestConfig;

class RestRuntimeConfigurationProvider implements ConfigurationProvider {
    private final RestConfig restConfig;

    RestRuntimeConfigurationProvider(RestConfig restConfig) {
        this.restConfig = restConfig;
    }

    @Override
    public MapNode provide() {
        String baseUrl = String.format("${runtime.web.baseUrl}%s", restConfig.getPath());
        return new MapNode(new NamedNode("runtime", new MapNode(
                new NamedNode("rest", new MapNode(
                        new NamedNode("baseUrl", baseUrl),
                        new NamedNode("baseUrlSlash", baseUrl + "/")
                )))));
    }
}
