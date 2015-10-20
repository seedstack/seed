/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.undertow.internal;

import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.core.NuunCore;
import io.undertow.Undertow;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.spec.ServletContextImpl;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.application.SeedConfigLoader;
import org.seedstack.seed.spi.SeedLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class UndertowLauncher implements SeedLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(UndertowLauncher.class);

    private DeploymentManager manager;
    private Kernel kernel;
    private Undertow undertow;

    @Override
    public void launch(String[] args) throws Exception {
        // Read the config from Seed bootstrap configuration
        DeploymentManagerFactory factory = new DeploymentManagerFactory();
        manager = factory.createDeploymentManager(new SeedConfigLoader().buildBootstrapConfig());
        manager.deploy();
        ServletContextImpl servletContext = manager.getDeployment().getServletContext();

        try {
            kernel = createKernel(servletContext);
            kernel.init();
            kernel.start();

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
            throw new IllegalStateException("Missing Undertow plugin at startup");
        }
        return undertowPlugin;
    }

    private static Kernel createKernel(ServletContext servletContext) {
        List<String> params = new ArrayList<String>();
        Enumeration<?> initparams = servletContext.getInitParameterNames();
        while (initparams.hasMoreElements()) {
            String keyName = (String) initparams.nextElement();
            if (keyName != null && !keyName.isEmpty()) {
                String value = servletContext.getInitParameter(keyName);
                LOGGER.debug("Setting kernel parameter {} to {}", keyName, value);
                params.add(keyName);
                params.add(value);
            }
        }

        return NuunCore.createKernel(NuunCore.newKernelConfiguration().containerContext(servletContext).params(params.toArray(new String[params.size()])));
    }

    @Override
    public void shutdown() throws Exception {
        if (kernel != null && kernel.isStarted()) {
            kernel.stop();
        }

        if (undertow != null) {
            undertow.stop();
        }

        if (manager != null) {
            // should done at last for diagnostic purpose
            manager.undeploy();
        }
    }

}
