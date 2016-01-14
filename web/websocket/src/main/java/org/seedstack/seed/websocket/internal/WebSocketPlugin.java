/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.websocket.internal;


import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.BindingRequest;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.websocket.ClientEndpoint;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This plugin scan Endpoint, ClientEndpoint and ServerEndpoint defined in the JSR 356.
 * All the scanned classes will be passed to the module.
 *
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 18/12/13
 */
public class WebSocketPlugin extends AbstractPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketPlugin.class);

    private final Set<Class<?>> serverEndpointClasses = new HashSet<Class<?>>();

    private ServerContainer serverContainer;

    @Override
    public String name() {
        return "websocket";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().annotationType(ServerEndpoint.class).build();
    }

    @Override
    public Collection<BindingRequest> bindingRequests() {
        return bindingRequestsBuilder().annotationType(ClientEndpoint.class).build();
    }

    @Override
    public InitState init(InitContext initContext) {
        Map<Class<? extends Annotation>, Collection<Class<?>>> scannedClassesByAnnotationClass = initContext.scannedClassesByAnnotationClass();

        if (this.serverContainer != null) {
            serverEndpointClasses.addAll(scannedClassesByAnnotationClass.get(ServerEndpoint.class));
        } else {
            LOGGER.info("JSR 356 WebSocket support is not available");
        }

        return InitState.INITIALIZED;
    }

    @Override
    public void start(Context context) {
        for (Class<?> endpointClass : serverEndpointClasses) {
            try {
                serverContainer.addEndpoint(endpointClass);
            } catch (DeploymentException e) {
                throw new PluginException("Unable to deploy WebSocket server endpoint " + endpointClass, e);
            }
        }
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        if (containerContext != null && ServletContext.class.isAssignableFrom(containerContext.getClass())) {
            this.serverContainer = (ServerContainer) ((ServletContext) containerContext).getAttribute("javax.websocket.server.ServerContainer");
        }
    }

    @Override
    public Object nativeUnitModule() {
        return new WebSocketModule(serverEndpointClasses);
    }
}
