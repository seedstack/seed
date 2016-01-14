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
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.application.SeedConfigLoader;
import org.seedstack.seed.spi.SeedLauncher;
import org.seedstack.seed.web.listener.SeedServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class UndertowLauncher implements SeedLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(UndertowLauncher.class);

    private DeploymentManager manager;
    private Undertow undertow;

    @Override
    public void launch(String[] args) throws Exception {
        DeploymentManagerFactory factory = new DeploymentManagerFactory();
        manager = factory.createDeploymentManager(new SeedConfigLoader().buildBootstrapConfig());

        manager.deploy();

        ServletContextImpl servletContext = manager.getDeployment().getServletContext();
        Kernel kernel = (Kernel) servletContext.getAttribute(SeedServletContextListener.KERNEL_ATTRIBUTE_NAME);

        try {
            ServerConfig serverConfig = getUndertowPlugin(kernel).getServerConfig();
            undertow = new ServerFactory().createServer(serverConfig, manager);
            undertow.start();

            LOGGER.info("Listening on " + serverConfig.getHost() + ":" + serverConfig.getPort());
        } catch (SeedException e) {
            throw e;
        } catch (Exception e) {
            throw SeedException.wrap(e, UndertowErrorCode.UNEXPECTED_EXCEPTION);
        }
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
        }

        if (manager != null) {
            // should done at last for diagnostic purpose
            manager.undeploy();
        }
    }
}
