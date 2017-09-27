/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.undertow.internal;

import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.util.ImmediateInstanceHandle;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import javax.servlet.ServletContainerInitializer;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.web.WebConfig;

class DeploymentManagerFactory {
    DeploymentManager createDeploymentManager(Coffig baseConfiguration) {
        WebConfig.ServerConfig serverConfig = baseConfiguration.get(WebConfig.ServerConfig.class);
        DeploymentInfo servletBuilder = configureDeploymentInfo(serverConfig.getContextPath());
        return Servlets.defaultContainer().addDeployment(servletBuilder);
    }

    private DeploymentInfo configureDeploymentInfo(String contextPath) {
        DeploymentInfo deploymentInfo = Servlets.deployment()
                .setClassLoader(UndertowLauncher.class.getClassLoader())
                .setDeploymentName("app.war")
                .setContextPath(contextPath);

        for (ServletContainerInitializer servletContainerInitializer : loadServletContainerInitializers()) {
            deploymentInfo.addServletContainerInitalizer(
                    createServletContainerInitializerInfo(servletContainerInitializer));
        }

        return deploymentInfo;
    }

    private <T extends ServletContainerInitializer> ServletContainerInitializerInfo
    createServletContainerInitializerInfo(
            final T servletContainerInitializer) {
        return new ServletContainerInitializerInfo(servletContainerInitializer.getClass(),
                () -> new ImmediateInstanceHandle<>(servletContainerInitializer), null);
    }

    private Set<ServletContainerInitializer> loadServletContainerInitializers() {
        Set<ServletContainerInitializer> servletContainerInitializers = new HashSet<>();
        for (ServletContainerInitializer servletContainerInitializer : ServiceLoader.load(
                ServletContainerInitializer.class)) {
            servletContainerInitializers.add(servletContainerInitializer);
        }
        return servletContainerInitializers;
    }
}
