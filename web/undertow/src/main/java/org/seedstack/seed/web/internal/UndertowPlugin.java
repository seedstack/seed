/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import io.undertow.Undertow;
import io.undertow.servlet.api.DeploymentManager;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.crypto.internal.CryptoPlugin;
import org.seedstack.seed.crypto.spi.SSLConfiguration;
import org.seedstack.seed.crypto.spi.SSLProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The Undertow plugin is responsible to start and stop the Undertow HTTP server.
 * <p>
 * It requires that a {@link io.undertow.servlet.api.DeploymentManager} being passed
 * using the setDeploymentManager() method. Otherwise the server won't be started.
 * </p>
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class UndertowPlugin extends AbstractPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(UndertowPlugin.class);
    private static final String UNDERTOW_PLUGIN_PREFIX = "org.seedstack.seed.server";
    private DeploymentManager deploymentManager;
    private Undertow server;
    private ServerConfig serverConfig;

    @Override
    public String name() {
        return "undertow-plugin";
    }

    @Override
    public InitState init(InitContext initContext) {
        Configuration configuration = null;
        SSLConfiguration SSLConfiguration = null;
        SSLContext sslContext = null;
        for (Plugin plugin : initContext.pluginsRequired()) {
            if (plugin instanceof ApplicationPlugin) {
                configuration = ((ApplicationPlugin) plugin).getApplication().getConfiguration();
            } else if (SSLProvider.class.isAssignableFrom(plugin.getClass())) {
                SSLProvider sslProvider = (SSLProvider) plugin;
                SSLConfiguration = sslProvider.sslConfig();
                sslContext = sslProvider.sslContext();
            } else {
                throw SeedException.createNew(UndertowErrorCode.UNEXPECTED_EXCEPTION);
            }
        }
        if (configuration == null) {
            throw SeedException.createNew(UndertowErrorCode.UNEXPECTED_EXCEPTION);
        }

        serverConfig = new ServerConfigFactory().create(configuration.subset(UNDERTOW_PLUGIN_PREFIX), SSLConfiguration, sslContext);

        return InitState.INITIALIZED;
    }

    @Override
    public void start(Context context) {
        if (deploymentManager != null) {
            // Start the embedded server
            server = new ServerFactory().createServer(serverConfig, deploymentManager);
            server.start();
            LOGGER.info("Application started. Listen on " + serverConfig.getHost() + ":" + serverConfig.getPort());
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Sets the deployment manager responsible of the ServletContainer.
     *
     * @param deploymentManager the deployment manager
     */
    public void setDeploymentManager(DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        plugins.add(CryptoPlugin.class);
        return plugins;
    }

}
