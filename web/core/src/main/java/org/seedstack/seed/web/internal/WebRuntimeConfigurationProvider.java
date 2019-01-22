/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import static org.seedstack.shed.reflect.ReflectUtils.invoke;

import java.lang.reflect.Method;
import javax.servlet.ServletContext;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationProvider;

class WebRuntimeConfigurationProvider implements ConfigurationProvider {
    private final ServletContext servletContext;

    WebRuntimeConfigurationProvider(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public MapNode provide() {
        MapNode info = new MapNode(
                new NamedNode("contextPath", servletContext.getContextPath())
        );

        if (servletContext.getMajorVersion() > 3
                || servletContext.getMajorVersion() == 3 && servletContext.getMinorVersion() >= 1) {
            try {
                Method getVirtualServerName = ServletContext.class.getMethod("getVirtualServerName");
                info.set("virtualServerName", new ValueNode((String) invoke(getVirtualServerName, servletContext)));
            } catch (NoSuchMethodException | NoSuchMethodError e) {
                // ignore as this method doesn't exists below Servlet 3.1
            }
        }

        return new MapNode(new NamedNode("runtime", new MapNode(
                new NamedNode("web", new MapNode(
                        new NamedNode("servlet", info)
                ))
        )));
    }
}
