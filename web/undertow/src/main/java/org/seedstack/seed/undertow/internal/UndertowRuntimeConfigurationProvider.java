/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.undertow.internal;

import javax.servlet.ServletContext;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.spi.ConfigurationProvider;
import org.seedstack.seed.web.WebConfig;

class UndertowRuntimeConfigurationProvider implements ConfigurationProvider {
    private static final String LOCALHOST = "localhost";
    private final ServletContext servletContext;
    private final WebConfig.ServerConfig serverConfig;

    UndertowRuntimeConfigurationProvider(ServletContext servletContext, WebConfig.ServerConfig serverConfig) {
        this.servletContext = servletContext;
        this.serverConfig = serverConfig;
    }

    @Override
    public MapNode provide() {
        String protocol;
        int port;
        if (serverConfig.isHttps() && (!serverConfig.isHttp() || serverConfig.isPreferHttps())) {
            protocol = "https";
            port = serverConfig.getSecurePort();
        } else {
            protocol = "http";
            port = serverConfig.getPort();
        }
        String baseUrl = String.format("%s://%s:%d%s", protocol, LOCALHOST, port, servletContext.getContextPath());

        return new MapNode(new NamedNode("runtime", new MapNode(
                new NamedNode("web", new MapNode(
                        new NamedNode("server", new MapNode(
                                new NamedNode("protocol", protocol),
                                new NamedNode("host", LOCALHOST),
                                new NamedNode("port", String.valueOf(port))
                        )),
                        new NamedNode("baseUrl", baseUrl),
                        new NamedNode("baseUrlSlash", baseUrl + "/")
                )))));
    }
}
