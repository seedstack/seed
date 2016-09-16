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
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.provider.EnvironmentVariableProvider;
import org.seedstack.coffig.provider.JacksonProvider;
import org.seedstack.coffig.provider.SystemPropertyProvider;
import org.seedstack.seed.DiagnosticManager;
import org.seedstack.seed.LogConfig;
import org.seedstack.seed.core.internal.configuration.PrioritizedProvider;
import org.seedstack.seed.core.internal.init.ConsoleManager;
import org.seedstack.seed.core.internal.init.DiagnosticManagerImpl;
import org.seedstack.seed.core.internal.init.LogbackLogManager;
import org.seedstack.seed.core.internal.init.NoOpLogManager;
import org.seedstack.seed.core.internal.init.NuunManager;
import org.seedstack.seed.core.utils.SeedLoggingUtils;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.spi.log.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.annotation.Nullable;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    private volatile static boolean initialized;

    private final ConcurrentMap<String, DiagnosticManager> diagnosticManagers = new ConcurrentHashMap<>();
    private final Coffig baseConfiguration;
    private final ValidatorFactory validatorFactory;
    private final ConsoleManager consoleManager;
    private final LogManager logManager;
    private final NuunManager nuunManager;

    static {
        try {
            java.util.logging.LogManager.getLogManager().reset();
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
        } catch (Exception e) {
            SeedLoggingUtils.logWarningWithDebugDetails(LOGGER, e, "Unable to redirect JUL loggers to SLF4J");
        }
    }

    // no direct instantiation allowed
    private Seed() {
        consoleManager = new ConsoleManager();
        consoleManager.install();

        logManager = buildLogManager();
        validatorFactory = buildValidatorFactory();
        baseConfiguration = buildBaseConfiguration();
        logManager.init(baseConfiguration.get(LogConfig.class));

        nuunManager = new NuunManager();
        nuunManager.init();

        initialized = true;
    }

    public static void close() {
        if (initialized) {
            Holder.INSTANCE.logManager.close();
            Holder.INSTANCE.consoleManager.uninstall();
            Holder.INSTANCE.validatorFactory.close();
            SLF4JBridgeHandler.uninstall();
        }
    }

    public static Kernel createKernel() {
        return createKernel(null, null, true);
    }

    public static Kernel createKernel(@Nullable Object runtimeContext, @Nullable KernelConfiguration kernelConfiguration, boolean autoStart) {
        // Global context
        DiagnosticManagerImpl diagnosticManager = new DiagnosticManagerImpl();
        SeedRuntime seedRuntime = SeedRuntime.builder()
                .context(runtimeContext)
                .diagnosticManager(diagnosticManager)
                .configuration(Holder.INSTANCE.baseConfiguration.fork())
                .consoleManager(Holder.INSTANCE.consoleManager)
                .validatorFactory(Holder.INSTANCE.validatorFactory)
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

    public static void disposeKernel(Kernel kernel) {
        if (kernel == null) {
            return;
        }

        if (kernel.isStarted()) {
            LOGGER.info("Stopping Seed");
            kernel.stop();
            LOGGER.info("<<< Seed stopped");
        }

        Holder.INSTANCE.unregisterDiagnosticManager(kernel.name());
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
                .enableValidation(validatorFactory)
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

    private ValidatorFactory buildValidatorFactory() {
        return Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();
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

    private LogManager buildLogManager() {
        if (isLogbackInUse()) {
            return new LogbackLogManager();
        } else {
            return new NoOpLogManager();
        }
    }

    private boolean isLogbackInUse() {
        return SeedReflectionUtils.isClassPresent("ch.qos.logback.classic.LoggerContext") && LoggerFactory.getILoggerFactory() instanceof LoggerContext;
    }
}
