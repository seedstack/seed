/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.internal.web;

import org.seedstack.seed.core.utils.SeedReflectionUtils;
import com.sun.xml.ws.api.ResourceLoader;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.transport.http.servlet.ServletModule;

import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class SeedServletContainer extends Container {
    private static final String SECURITY_SERVER_TUBE_CLASS_NAME = "com.sun.xml.wss.jaxws.impl.SecurityServerTube";
    private static final String WSIT_SERVER_AUTH_CONTEXT_CLASS_NAME = "com.sun.xml.wss.provider.wsit.WSITServerAuthContext";
    private static final String GET_REALM_AUTHENTICATION_ADAPTER_METHOD_NAME = "getRealmAuthenticationAdapter";

    private final ServletContext servletContext;

    private final ServletModule module = new ServletModule() {
        private final List<BoundEndpoint> endpoints = new ArrayList<BoundEndpoint>();

        @Override
        public List<BoundEndpoint> getBoundEndpoints() {
            return endpoints;
        }

        @Override
        public String getContextPath() {
            // Cannot compute this since we don't know about hostname and port etc
            throw new WebServiceException("Container " + SeedServletContainer.class.getName() + " doesn't support getContextPath()");
        }
    };

    private final ResourceLoader loader = new ResourceLoader() {
        @Override
        public URL getResource(String resource) throws MalformedURLException {
            return servletContext.getResource("/WEB-INF/" + resource);
        }
    };

    SeedServletContainer(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public <T> T getSPI(Class<T> spiType) {
        if (spiType == ServletContext.class) {
            if (isCalledForRealmAuthenticationAdapter(SeedReflectionUtils.findCaller(this))) {
                // Force the RealmAuthenticationAdapter to be searched from classpath instead of weird webapp location
                return null;
            }

            return spiType.cast(servletContext);
        }
        if (spiType.isAssignableFrom(ServletModule.class)) {
            return spiType.cast(module);
        }
        if (spiType == ResourceLoader.class) {
            return spiType.cast(loader);
        }

        return null;
    }

    private boolean isCalledForRealmAuthenticationAdapter(StackTraceElement stackTrace) {
        return stackTrace != null &&
                (WSIT_SERVER_AUTH_CONTEXT_CLASS_NAME.equals(stackTrace.getClassName()) || SECURITY_SERVER_TUBE_CLASS_NAME.equals(stackTrace.getClassName())) &&
                GET_REALM_AUTHENTICATION_ADAPTER_METHOD_NAME.equals(stackTrace.getMethodName());

    }
}
