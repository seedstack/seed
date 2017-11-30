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
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.spi.SeedLauncher;
import org.seedstack.seed.undertow.UndertowConfig;
import org.seedstack.seed.web.WebConfig;
import org.seedstack.seed.web.internal.ServletContextUtils;
import org.seedstack.shed.exception.BaseException;
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
    }

    @Override
    public void shutdown() throws Exception {
        stop();
        undeploy();
    }

    @Override
    public void refresh() throws Exception {
        LOGGER.info("Refreshing Web application");
        stop();
        undeploy();

        Seed.refresh();

        WebConfig.ServerConfig serverConfig = Seed.baseConfiguration().get(WebConfig.ServerConfig.class);
        UndertowConfig undertowConfig = Seed.baseConfiguration().get(UndertowConfig.class);

        deploy(serverConfig);
        start(serverConfig, undertowConfig);
    }

    private void deploy(WebConfig.ServerConfig serverConfig) throws Exception {
        try {
            DeploymentManagerFactory factory = new DeploymentManagerFactory(serverConfig);
            deploymentManager = factory.createDeploymentManager();
            deploymentManager.deploy();
            deploymentManager.start();
        } catch (Exception e) {
            handleUndertowException(e);
        }
    }

    private void undeploy() throws Exception {
        if (deploymentManager != null) {
            try {
                deploymentManager.stop();
                deploymentManager.undeploy();
            } catch (Exception e) {
                handleUndertowException(e);
            } finally {
                deploymentManager = null;
            }
        }
    }

    private void start(WebConfig.ServerConfig serverConfig, UndertowConfig undertowConfig) throws Exception {
        try {
            UndertowPlugin undertowPlugin = getUndertowPlugin(deploymentManager);
            undertow = new ServerFactory(serverConfig, undertowConfig).createServer(
                    deploymentManager,
                    undertowPlugin.getSslProvider()
            );
            undertow.start();
            LOGGER.info("Undertow Web server listening on {}:{}", serverConfig.getHost(), serverConfig.getPort());
        } catch (Exception e) {
            handleUndertowException(e);
        }
    }

    private void stop() throws Exception {
        if (undertow != null) {
            try {
                undertow.stop();
                LOGGER.info("Undertow Web server stopped");
            } catch (Exception e) {
                handleUndertowException(e);
            } finally {
                undertow = null;
            }
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

    private void handleUndertowException(Exception e) throws Exception {
        // Undertow always wraps exception in a RuntimeException
        if (e instanceof RuntimeException && e.getCause() instanceof BaseException) {
            throw (BaseException) e.getCause();
        } else {
            throw e;
        }
    }
}
