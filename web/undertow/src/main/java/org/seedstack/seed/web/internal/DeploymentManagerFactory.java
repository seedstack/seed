/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.FilterInfo;
import org.apache.commons.configuration.Configuration;

import javax.servlet.DispatcherType;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class DeploymentManagerFactory {

    public static final String DEFAULT_CONTEXT_PATH = "/";

    public DeploymentManager createDeploymentManager(Configuration bootstrapConfig) {
        String contextPath = bootstrapConfig.getString("server.context-path", DEFAULT_CONTEXT_PATH);
        DeploymentInfo servletBuilder = configureDeploymentInfo(contextPath);
        return Servlets.defaultContainer().addDeployment(servletBuilder);
    }

    private DeploymentInfo configureDeploymentInfo(String contextPath) {
        return Servlets.deployment()
                .setClassLoader(UndertowLauncher.class.getClassLoader())
                .setDeploymentName("app.war")
                .setContextPath(contextPath)
                .addFilter(new FilterInfo("guiceFilter", com.google.inject.servlet.GuiceFilter.class))
                .addFilterUrlMapping("guiceFilter", "/*", DispatcherType.REQUEST);
    }
}
