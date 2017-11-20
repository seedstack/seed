/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.undertow.internal;

import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.Plugin;
import io.undertow.Undertow;
import io.undertow.servlet.api.DeploymentManager;
import javax.servlet.ServletException;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.spi.SeedLauncher;
import org.seedstack.seed.undertow.UndertowConfig;
import org.seedstack.seed.web.WebConfig;
import org.seedstack.seed.web.internal.ServletContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UndertowLauncher implements SeedLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(UndertowLauncher.class);
    private DeploymentManager deploymentManager;
    private Undertow undertow;

    @Override
    public void launch(String[] args) throws Exception {
        WebConfig.ServerConfig serverConfig = Seed.baseConfiguration().get(WebConfig.ServerConfig.class);
        UndertowConfig undertowConfig = Seed.baseConfiguration().get(UndertowConfig.class);

        // Create deployment
        deploy(serverConfig);

        // Start the HTTP server
        start(serverConfig, undertowConfig);

        LOGGER.info("Undertow Web server listening on {}:{}", serverConfig.getHost(), serverConfig.getPort());
    }

    @Override
    public void shutdown() throws Exception {
        stop();
        LOGGER.info("Undertow Web server stopped");
        undeploy();
    }

    @Override
    public void refresh() {
        LOGGER.info("Refreshing Web application");
        if (undertow != null && deploymentManager != null) {
            stop();
            undeploy();

            Seed.refresh();

            WebConfig.ServerConfig serverConfig = Seed.baseConfiguration().get(WebConfig.ServerConfig.class);
            UndertowConfig undertowConfig = Seed.baseConfiguration().get(UndertowConfig.class);

            deploy(serverConfig);
            start(serverConfig, undertowConfig);

            LOGGER.info("Refresh complete, Undertow Web server listening on {}:{}", serverConfig.getHost(),
                    serverConfig.getPort());
        }
    }

    private void deploy(WebConfig.ServerConfig serverConfig) {
        DeploymentManagerFactory factory = new DeploymentManagerFactory(serverConfig);
        deploymentManager = factory.createDeploymentManager();
        deploymentManager.deploy();
        try {
            deploymentManager.start();
        } catch (ServletException e) {
            LOGGER.error("An error occurred when trying to start the servlet context", e);
        }
    }

    private void undeploy() {
        if (deploymentManager != null) {
            try {
                deploymentManager.stop();
            } catch (ServletException e) {
                LOGGER.error("An error occurred when trying to stop the servlet context", e);
            }
            deploymentManager.undeploy();
            deploymentManager = null;
        }
    }

    private void start(WebConfig.ServerConfig serverConfig, UndertowConfig undertowConfig) {
        UndertowPlugin undertowPlugin = getUndertowPlugin(deploymentManager);
        undertow = new ServerFactory(serverConfig, undertowConfig).createServer(
                deploymentManager,
                undertowPlugin.getSslProvider()
        );
        undertow.start();
    }

    private void stop() {
        if (undertow != null) {
            undertow.stop();
            undertow = null;
        }
    }

    private UndertowPlugin getUndertowPlugin(DeploymentManager deploymentManager) {
        Kernel kernel = ServletContextUtils.getKernel(deploymentManager.getDeployment().getServletContext());
        if (kernel != null) {
            Plugin plugin = kernel.plugins().get(UndertowPlugin.NAME);
            if (plugin instanceof UndertowPlugin) {
                return (UndertowPlugin) plugin;
            }
        }
        throw SeedException.createNew(UndertowErrorCode.MISSING_UNDERTOW_PLUGIN);
    }
}
