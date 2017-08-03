/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
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
import io.undertow.servlet.spec.ServletContextImpl;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.spi.SeedLauncher;
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
        Coffig baseConfiguration = Seed.baseConfiguration();

        DeploymentManagerFactory factory = new DeploymentManagerFactory();
        deploymentManager = factory.createDeploymentManager(baseConfiguration);
        deploymentManager.deploy();

        ServletContextImpl servletContext = deploymentManager.getDeployment().getServletContext();
        Kernel kernel = ServletContextUtils.getKernel(servletContext);
        UndertowPlugin undertowPlugin = getUndertowPlugin(kernel);
        WebConfig.ServerConfig serverConfig = undertowPlugin.getServerConfig();

        undertow = new ServerFactory().createServer(
                deploymentManager,
                serverConfig,
                undertowPlugin.getUndertowConfig(),
                undertowPlugin.getSslProvider()
        );
        undertow.start();
        LOGGER.info("Undertow Web server listening on {}:{}", serverConfig.getHost(), serverConfig.getPort());
    }

    private UndertowPlugin getUndertowPlugin(Kernel kernel) {
        UndertowPlugin undertowPlugin = null;
        Plugin plugin = kernel.plugins().get(UndertowPlugin.NAME);
        if (plugin instanceof UndertowPlugin) {
            undertowPlugin = (UndertowPlugin) plugin;
        }
        if (undertowPlugin == null) {
            throw SeedException.createNew(UndertowErrorCode.MISSING_UNDERTOW_PLUGIN);
        }
        return undertowPlugin;
    }

    @Override
    public void shutdown() throws Exception {
        if (undertow != null) {
            undertow.stop();
            LOGGER.info("Undertow Web server stopped");
        }
        if (deploymentManager != null) {
            // should done at last for diagnostic purpose
            deploymentManager.undeploy();
        }
    }
}
