/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.websocket;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.websocket.ClientEndpoint;
import javax.websocket.server.ServerEndpoint;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.web.spi.FilterDefinition;
import org.seedstack.seed.web.spi.ListenerDefinition;
import org.seedstack.seed.web.spi.ServletDefinition;
import org.seedstack.seed.web.spi.WebProvider;
import org.seedstack.shed.reflect.Classes;

/**
 * This plugin scan Endpoint, ClientEndpoint and ServerEndpoint defined in the JSR 356.
 * All the scanned classes will be passed to the module.
 */
public class WebSocketPlugin extends AbstractSeedPlugin implements WebProvider {
    private final boolean webSocketPresent = Classes.optional("javax.websocket.server.ServerEndpoint").isPresent();
    private final Set<Class<?>> serverEndpointClasses = new HashSet<>();
    private final Set<Class<?>> clientEndpointClasses = new HashSet<>();
    private ServletContext servletContext;

    @Override
    public String name() {
        return "websocket";
    }

    @Override
    public void setup(SeedRuntime seedRuntime) {
        servletContext = seedRuntime.contextAs(ServletContext.class);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        if (isEnabled()) {
            return classpathScanRequestBuilder().annotationType(ServerEndpoint.class).annotationType(
                    ClientEndpoint.class).build();
        } else {
            return super.classpathScanRequests();
        }
    }

    @Override
    public InitState initialize(InitContext initContext) {
        if (isEnabled()) {
            serverEndpointClasses.addAll(initContext.scannedClassesByAnnotationClass().get(ServerEndpoint.class));
            clientEndpointClasses.addAll(initContext.scannedClassesByAnnotationClass().get(ClientEndpoint.class));
        }
        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        if (isEnabled()) {
            return new WebSocketModule(serverEndpointClasses, clientEndpointClasses);
        } else {
            return super.nativeUnitModule();
        }
    }

    @Override
    public List<ServletDefinition> servlets() {
        return null;
    }

    @Override
    public List<FilterDefinition> filters() {
        return null;
    }

    @Override
    public List<ListenerDefinition> listeners() {
        if (isEnabled()) {
            return Lists.newArrayList(new ListenerDefinition(WebSocketListener.class));
        } else {
            return null;
        }
    }

    private boolean isEnabled() {
        return webSocketPresent && servletContext != null;
    }
}
