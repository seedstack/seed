/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.websocket;


import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.BindingRequest;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.seedstack.seed.SeedRuntime;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.web.spi.FilterDefinition;
import org.seedstack.seed.web.spi.ListenerDefinition;
import org.seedstack.seed.web.spi.ServletDefinition;
import org.seedstack.seed.web.spi.WebProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.websocket.ClientEndpoint;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This plugin scan Endpoint, ClientEndpoint and ServerEndpoint defined in the JSR 356.
 * All the scanned classes will be passed to the module.
 *
 * @author pierre.thirouin@ext.mpsa.com
 */
public class WebSocketPlugin extends AbstractPlugin implements WebProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketPlugin.class);

    private final boolean webSocketPresent = SeedReflectionUtils.isClassPresent("javax.websocket.server.ServerEndpoint");
    private final Set<Class<?>> serverEndpointClasses = new HashSet<Class<?>>();
    private ServletContext servletContext;

    @Override
    public String name() {
        return "websocket";
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        servletContext = ((SeedRuntime) containerContext).contextAs(ServletContext.class);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        if (isEnabled()) {
            return classpathScanRequestBuilder().annotationType(ServerEndpoint.class).build();
        } else {
            return super.classpathScanRequests();
        }
    }

    @Override
    public Collection<BindingRequest> bindingRequests() {
        if (isEnabled()) {
            return bindingRequestsBuilder().annotationType(ClientEndpoint.class).build();
        } else {
            return super.bindingRequests();
        }
    }

    @Override
    public InitState init(InitContext initContext) {
        if (isEnabled()) {
            serverEndpointClasses.addAll(initContext.scannedClassesByAnnotationClass().get(ServerEndpoint.class));
        }
        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        if (isEnabled()) {
            return new WebSocketModule(serverEndpointClasses);
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
            return Lists.newArrayList(new ListenerDefinition(WebSocketServletContextListener.class));
        } else {
            return null;
        }
    }

    private boolean isEnabled() {
        return webSocketPresent && servletContext != null;
    }
}
