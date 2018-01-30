/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.undertow.internal;

import io.nuun.kernel.api.Kernel;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.DeploymentManager;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.spi.SeedLauncher;
import org.seedstack.seed.undertow.UndertowConfig;
import org.seedstack.seed.web.WebConfig;
import org.seedstack.seed.web.internal.ServletContextUtils;
import org.seedstack.shed.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

public class UndertowLauncher implements SeedLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(UndertowLauncher.class);
    private final AtomicBoolean launched = new AtomicBoolean(false);
    private XnioWorker xnioWorker;
    private DeploymentManager deploymentManager;
    private Undertow undertow;
    private HttpHandler httpHandler;
    private Map<String, String> kernelParameters;

    @Override
    public void launch(String[] args, Map<String, String> kernelParameters) throws Exception {
        if (launched.compareAndSet(false, true)) {
            this.kernelParameters = Collections.unmodifiableMap(kernelParameters);
            startAll();
        } else {
            throw SeedException.createNew(UndertowErrorCode.UNDERTOW_ALREADY_LAUNCHED);
        }
    }

    @Override
    public void shutdown() throws Exception {
        if (launched.compareAndSet(true, false)) {
            try {
                stopAll();
            } finally {
                this.kernelParameters = null;
            }
        } else {
            throw SeedException.createNew(UndertowErrorCode.UNDERTOW_NOT_LAUNCHED);
        }
    }

    @Override
    public void refresh() throws Exception {
        if (launched.get()) {
            LOGGER.info("Refreshing Web application");
            stopAll();
            Seed.refresh();
            startAll();
        } else {
            throw SeedException.createNew(UndertowErrorCode.UNDERTOW_NOT_LAUNCHED);
        }
    }

    public Optional<Kernel> getKernel() {
        return Optional.ofNullable(deploymentManager)
                .map(DeploymentManager::getDeployment)
                .map(Deployment::getServletContext)
                .map(ServletContextUtils::getKernel);
    }

    private void startAll() throws Exception {
        Coffig baseConfiguration = Seed.baseConfiguration();
        createWorker(baseConfiguration);
        deploy(baseConfiguration);
        start();
    }

    private void stopAll() throws Exception {
        stop();
        undeploy();
        shutdownWorker();
    }

    private void createWorker(Coffig config) throws Exception {
        UndertowConfig undertowConfig = config.get(UndertowConfig.class);
        try {
            xnioWorker = Xnio.getInstance().createWorker(OptionMap.builder()
                    .set(Options.WORKER_IO_THREADS, undertowConfig.getIoThreads())
                    .set(Options.WORKER_TASK_CORE_THREADS, undertowConfig.getWorkerThreads())
                    .set(Options.WORKER_TASK_MAX_THREADS, undertowConfig.getWorkerThreads())
                    .set(Options.TCP_NODELAY, true)
                    .getMap());
        } catch (Exception e) {
            handleUndertowException(e);
        }
    }

    private void shutdownWorker() throws Exception {
        try {
            if (xnioWorker != null) {
                xnioWorker.shutdownNow();
                xnioWorker.awaitTermination(2, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            handleUndertowException(e);
        } finally {
            xnioWorker = null;
        }
    }

    private void deploy(Coffig configuration) throws Exception {
        try {
            DeploymentManagerFactory factory = new DeploymentManagerFactory(
                    xnioWorker,
                    configuration,
                    kernelParameters
            );
            deploymentManager = factory.createDeploymentManager();
            deploymentManager.deploy();
            httpHandler = deploymentManager.start();
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
                httpHandler = null;
                deploymentManager = null;
            }
        }
    }

    private void start() throws Exception {
        try {
            UndertowPlugin undertowPlugin = getUndertowPlugin();
            WebConfig.ServerConfig serverConfig = undertowPlugin.getServerConfig();
            undertow = new ServerFactory(xnioWorker, serverConfig).createServer(
                    httpHandler,
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

    private UndertowPlugin getUndertowPlugin() {
        return getKernel()
                .map(kernel -> kernel.plugins().get(UndertowPlugin.NAME))
                .filter(plugin -> plugin instanceof UndertowPlugin)
                .map(plugin -> (UndertowPlugin) plugin)
                .orElseThrow(() -> SeedException.createNew(UndertowErrorCode.MISSING_UNDERTOW_PLUGIN));
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
