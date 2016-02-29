/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import ch.qos.logback.classic.LoggerContext;
import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.config.KernelConfiguration;
import io.nuun.kernel.core.NuunCore;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.evaluator.FunctionEvaluator;
import org.seedstack.coffig.evaluator.MacroEvaluator;
import org.seedstack.coffig.processor.RemovalProcessor;
import org.seedstack.coffig.provider.EnvironmentVariableProvider;
import org.seedstack.coffig.provider.JacksonProvider;
import org.seedstack.coffig.provider.PrioritizedProvider;
import org.seedstack.coffig.provider.SystemPropertyProvider;
import org.seedstack.seed.DiagnosticManager;
import org.seedstack.seed.SeedRuntime;
import org.seedstack.seed.core.internal.init.ConsoleManager;
import org.seedstack.seed.core.internal.init.DiagnosticManagerImpl;
import org.seedstack.seed.core.internal.init.LogbackManager;
import org.seedstack.seed.core.internal.init.NuunManager;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.LogManager;

/**
 * This class is the Seed framework entry point, which is used create and dispose kernels. It handles global
 * initialization and shutdown automatically.
 *
 * @author adrien.lauer@mpsa.com
 */
public class Seed {
    private static final Logger LOGGER = LoggerFactory.getLogger(Seed.class);
    private static final String BASE_CONFIGURATION_PROVIDER = "base";
    private static final String BASE_OVERRIDE_CONFIGURATION_PROVIDER = "base-override";
    private static final String ENV_CONFIGURATION_PROVIDER = "env";
    private static final String SYS_CONFIGURATION_PROVIDER = "sys";
    public static final int CONFIGURATION_BASE_PRIORITY = 0;
    public static final int CONFIGURATION_OVERRIDE_PRIORITY = 1000;
    private static final int CONFIGURATION_ENVIRONMENT_PRIORITY = 2000;
    private static final int CONFIGURATION_SYS_PRIORITY = 3000;

    private static class Holder {
        private static final Seed INSTANCE = new Seed();
    }

    private final ConcurrentMap<String, DiagnosticManager> diagnosticManagers = new ConcurrentHashMap<>();
    private final Coffig baseConfiguration = buildBaseConfiguration();
    private int initializationCount = 0;
    private ConsoleManager consoleManager;
    private LogbackManager logbackManager;
    private NuunManager nuunManager;

    // no direct instantiation allowed
    private Seed() {
    }

    public static Kernel createKernel() {
        return createKernel(null, null, true);
    }

    public static Kernel createKernel(@Nullable Object runtimeContext, @Nullable KernelConfiguration kernelConfiguration, boolean autoStart) {
        synchronized (Holder.INSTANCE) {
            Holder.INSTANCE.init();

            // Global context
            DiagnosticManagerImpl diagnosticManager = new DiagnosticManagerImpl();
            SeedRuntime seedRuntime = SeedRuntime.builder()
                    .context(runtimeContext)
                    .diagnosticManager(diagnosticManager)
                    .configuration(Holder.INSTANCE.baseConfiguration.fork())
                    .colorSupported(Holder.INSTANCE.consoleManager.isColorSupported())
                    .build();

            // Startup message
            StringBuilder startMessage = new StringBuilder(">>> Starting Seed");
            if (seedRuntime.getVersion() != null) {
                startMessage.append(" v").append(seedRuntime.getVersion());
            }
            LOGGER.info(startMessage.toString());

            // Banner
            String banner = Holder.INSTANCE.getBanner();
            if (banner != null) {
                System.out.println(banner);
            }

            // Kernel
            if (kernelConfiguration == null) {
                kernelConfiguration = NuunCore.newKernelConfiguration();
            }
            kernelConfiguration.containerContext(seedRuntime);
            Kernel kernel = Holder.INSTANCE.nuunManager.initKernel(kernelConfiguration, diagnosticManager);
            Holder.INSTANCE.registerDiagnosticManager(kernel.name(), diagnosticManager);
            if (autoStart) {
                kernel.start();
                LOGGER.info("Seed started");
            }

            return kernel;
        }
    }

    public static void disposeKernel(Kernel kernel) {
        if (kernel == null) {
            return;
        }

        if (kernel.isStarted()) {
            LOGGER.info("Stopping Seed");
            kernel.stop();
            LOGGER.info("<<< Seed stopped");
        }

        synchronized (Holder.INSTANCE) {
            Holder.INSTANCE.unregisterDiagnosticManager(kernel.name());
            Holder.INSTANCE.shutdown();
        }
    }

    public static DiagnosticManager diagnostic() {
        return diagnostic(null);
    }

    public static DiagnosticManager diagnostic(@Nullable Kernel kernel) {
        if (kernel != null) {
            return Holder.INSTANCE.getDiagnosticManager(kernel.name());
        }

        return new DiagnosticManagerImpl();
    }

    public static Coffig baseConfiguration() {
        return Holder.INSTANCE.baseConfiguration;
    }

    synchronized private void init() {
        if (initializationCount == 0) {
            consoleManager = new ConsoleManager();
            consoleManager.install();

            LogManager.getLogManager().reset();
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
            if (isLogbackInUse()) {
                logbackManager = new LogbackManager(baseConfiguration);
                logbackManager.configure();
            }

            nuunManager = new NuunManager();
            nuunManager.configure();

            initializationCount++;
        }
    }

    synchronized private void shutdown() {
        if (initializationCount > 0) {
            if (nuunManager != null) {
                nuunManager.restore();
                nuunManager = null;
            }

            if (isLogbackInUse() && logbackManager != null) {
                logbackManager.close();
                logbackManager = null;
            }
            SLF4JBridgeHandler.uninstall();

            if (consoleManager != null) {
                consoleManager.uninstall();
                consoleManager = null;
            }

            initializationCount--;
        }
    }

    private DiagnosticManager getDiagnosticManager(String name) {
        return diagnosticManagers.getOrDefault(name, new DiagnosticManagerImpl());
    }

    private void registerDiagnosticManager(String name, DiagnosticManager diagnosticManager) {
        diagnosticManagers.put(name, diagnosticManager);
    }

    private void unregisterDiagnosticManager(String name) {
        diagnosticManagers.remove(name);
    }

    private Coffig buildBaseConfiguration() {
        return Coffig.builder()
                .withProviders(new PrioritizedProvider()
                        .registerProvider(BASE_CONFIGURATION_PROVIDER, buildJacksonProvider("application.yaml"), CONFIGURATION_BASE_PRIORITY)
                        .registerProvider(BASE_OVERRIDE_CONFIGURATION_PROVIDER, buildJacksonProvider("application.override.yaml"), CONFIGURATION_OVERRIDE_PRIORITY)
                        .registerProvider(ENV_CONFIGURATION_PROVIDER, new EnvironmentVariableProvider(), CONFIGURATION_ENVIRONMENT_PRIORITY)
                        .registerProvider(SYS_CONFIGURATION_PROVIDER, new SystemPropertyProvider(), CONFIGURATION_SYS_PRIORITY))
                .withProcessors(new RemovalProcessor())
                .withEvaluators(new MacroEvaluator(), new FunctionEvaluator())
                .build();
    }

    private JacksonProvider buildJacksonProvider(String resourceName) {
        JacksonProvider jacksonProvider = new JacksonProvider();
        try {
            Enumeration<URL> configResources = SeedReflectionUtils.findMostCompleteClassLoader(Seed.class).getResources(resourceName);
            while (configResources.hasMoreElements()) {
                jacksonProvider.addSource(configResources.nextElement());
            }
        } catch (IOException e) {
            LOGGER.warn("I/O error during detection of configuration files", e);
        }
        return jacksonProvider;
    }

    private String getBanner() {
        InputStream bannerStream = SeedReflectionUtils.findMostCompleteClassLoader(Seed.class).getResourceAsStream("banner.txt");
        if (bannerStream != null) {
            try {
                return new Scanner(bannerStream).useDelimiter("\\Z").next();
            } finally {
                try {
                    bannerStream.close();
                } catch (IOException e) {
                    // nothing to do
                }
            }
        }

        return null;
    }

    private boolean isLogbackInUse() {
        return SeedReflectionUtils.isClassPresent("ch.qos.logback.classic.LoggerContext") && LoggerFactory.getILoggerFactory() instanceof LoggerContext;
    }
}
