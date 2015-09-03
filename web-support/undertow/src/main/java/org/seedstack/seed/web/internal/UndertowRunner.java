/*
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.FilterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public final class UndertowRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(UndertowRunner.class);

    private UndertowRunner() {
    }

    public static void main(String[] args) {
        LOGGER.info("Starting Seed Web application");
        String host = "localhost";
        int port = 2013;
        String contextPath = "/myapp";

        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(UndertowRunner.class.getClassLoader())
                .setDeploymentName("app.war")
                .setContextPath(contextPath)
                .addFilter(new FilterInfo("guiceFilter", com.google.inject.servlet.GuiceFilter.class))
                .addFilterUrlMapping("guiceFilter", "/*", DispatcherType.REQUEST);

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
        manager.deploy();
        PathHandler path = null;

        try {
            path = Handlers.path(Handlers.redirect(contextPath))
                    .addPrefixPath(contextPath, manager.start());
        } catch (ServletException e) {
            LOGGER.error(e.getMessage(), e);
        }

        Undertow server = Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(path)
                .build();
        server.start();

        LOGGER.info("Seed Web application started: listen on " + host + ":" + port);
    }
}
