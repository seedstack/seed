/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import javax.servlet.ServletContext;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.spi.ConfigurationProvider;

class WebRuntimeConfigurationProvider implements ConfigurationProvider {
    private final ServletContext servletContext;

    WebRuntimeConfigurationProvider(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public MapNode provide() {
        return new MapNode(new NamedNode("web", new MapNode(
                new NamedNode("runtime", new MapNode(
                        new NamedNode("servlet", new MapNode(
                                new NamedNode("contextPath", servletContext.getContextPath()),
                                new NamedNode("serverInfo", servletContext.getServerInfo()),
                                new NamedNode("virtualServerName", servletContext.getVirtualServerName())
                        ))
                ))
        )));
    }
}
