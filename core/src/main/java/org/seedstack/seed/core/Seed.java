/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.config.KernelConfiguration;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.ApplicationConfig;
import org.seedstack.seed.LoggingConfig;
import org.seedstack.seed.ProxyConfig;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.seedstack.seed.core.internal.diagnostic.DiagnosticManagerImpl;
import org.seedstack.seed.core.internal.init.AutodetectLogManager;
import org.seedstack.seed.core.internal.init.BaseConfiguration;
import org.seedstack.seed.core.internal.init.ConsoleManager;
import org.seedstack.seed.core.internal.init.GlobalValidatorFactory;
import org.seedstack.seed.core.internal.init.KernelManager;
import org.seedstack.seed.core.internal.init.LogManager;
import org.seedstack.seed.core.internal.init.ProxyManager;
import org.seedstack.seed.diagnostic.DiagnosticManager;
import org.seedstack.seed.spi.SeedInitializer;
import org.seedstack.shed.ClassLoaders;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Optional;
import java.util.Scanner;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * This class is the Seed framework entry point, which is used create and dispose kernels.
 * It handles global initialization and cleanup.
 */
public class Seed {
    private static final String WELCOME_MESSAGE = " ____                _ \n" +
            "/ ___|  ___  ___  __| |\n" +
            "\\___ \\ / _ \\/ _ \\/ _` |\n" +
            " ___) |  __/  __/ (_| |\n" +
            "|____/ \\___|\\___|\\____|";
    private static volatile boolean initialized = false;
    private static volatile boolean noLogs = false;
    private static final DiagnosticManager diagnosticManager = new DiagnosticManagerImpl();
    private static final String seedVersion = Optional.ofNullable(Seed.class.getPackage()).map(Package::getImplementationVersion).orElse(null);
    private final Coffig configuration;
    private final ConsoleManager consoleManager;
    private final LogManager logManager;
    private final ValidatorFactory validatorFactory;
    private final ProxyManager proxyManager;
    private final KernelManager kernelManager;
    private final Set<SeedInitializer> seedInitializers = new HashSet<>();

    private static class Holder {
        private static final Seed INSTANCE = new Seed();
    }

    /**
     * Disable logs globally if a supported SLF4J implementation is used. Currently only Logback is supported.
     */
    public static void disableLogs() {
        noLogs = true;
    }

    /**
     * Create and start a basic kernel without specifying a runtime context, nor a configuration. Seed JVM-global
     * state is automatically initialized before the first time a kernel is created.
     *
     * @return the {@link Kernel} instance.
     */
    public static Kernel createKernel() {
        return createKernel(null, null, true);
    }

    /**
     * Create, initialize and optionally start a kernel with the specified runtime context and configuration. Seed JVM-global
     * state is automatically initialized before the first time a kernel is created.
     *
     * @param runtimeContext      the runtime context object, which will be accessible from plugins.
     * @param kernelConfiguration the kernel configuration.
     * @param autoStart           if true, the kernel is started automatically.
     * @return the {@link Kernel} instance.
     */
    public static Kernel createKernel(@Nullable Object runtimeContext, @Nullable KernelConfiguration kernelConfiguration, boolean autoStart) {
        Seed instance = getInstance();
        return instance.kernelManager.createKernel(
                SeedRuntime.builder()
                        .context(runtimeContext)
                        .diagnosticManager(diagnosticManager)
                        .configuration(instance.configuration.fork())
                        .validatorFactory(instance.validatorFactory)
                        .version(seedVersion)
                        .build(),
                kernelConfiguration,
                autoStart);
    }

    /**
     * Stops and dispose a running {@link Kernel} instance.
     *
     * @param kernel the kernel to dispose.
     */
    public static void disposeKernel(Kernel kernel) {
        KernelManager.get().disposeKernel(kernel);
    }

    /**
     * Provides the default {@link DiagnosticManager} instance to dump diagnostics outside a running kernel.
     *
     * @return the default diagnostic manager.
     */
    public static DiagnosticManager diagnostic() {
        return diagnosticManager;
    }

    /**
     * Provides the application base configuration (i.e. not including configuration sources discovered after kernel
     * startup).
     *
     * @return the {@link Coffig} object for application base configuration.
     */
    public static Coffig baseConfiguration() {
        return getInstance().configuration;
    }

    /**
     * Cleanup Seed JVM-global state explicitly. Should be done before exiting the JVM. After calling this method
     * Seed is no longer usable in the current JVM.
     */
    public static void close() {
        if (initialized) {
            Seed instance = getInstance();
            instance.seedInitializers.forEach(SeedInitializer::onClose);
            instance.proxyManager.uninstall();
            instance.validatorFactory.close();
            instance.consoleManager.uninstall();
            instance.logManager.close();
            Thread.setDefaultUncaughtExceptionHandler(null);
            initialized = false;
        }
    }

    private static Seed getInstance() {
        try {
            return Holder.INSTANCE;
        } catch (Throwable t) {
            if (t instanceof ExceptionInInitializerError) {
                t = t.getCause();
            }
            throw SeedException.wrap(t, CoreErrorCode.UNABLE_TO_INITIALIZE_SEED);
        }
    }

    private Seed() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof SeedException) {
                throwable.printStackTrace(System.err);
            } else {
                SeedException.wrap(throwable, CoreErrorCode.UNEXPECTED_EXCEPTION).printStackTrace(System.err);
            }
        });

        // Logging initialization (should silence logs until logging activation later in the initialization)
        logManager = AutodetectLogManager.get();

        // Console
        consoleManager = ConsoleManager.get();
        consoleManager.install();

        // Validation
        validatorFactory = GlobalValidatorFactory.get();

        // Configuration
        configuration = BaseConfiguration.get();

        // Banner
        boolean versionPrinted = false;
        if (!noLogs && configuration.get(ApplicationConfig.class).isPrintBanner()) {
            String banner = getBanner();
            if (banner != null) {
                System.out.println(banner);
            } else {
                if (seedVersion == null) {
                    System.out.printf("%s%n%n", WELCOME_MESSAGE);
                } else {
                    System.out.printf("%s v%s%n%n", WELCOME_MESSAGE, seedVersion);
                    versionPrinted = true;
                }
            }
        }

        // Logging activation
        if (!noLogs) {
            logManager.configure(configuration.get(LoggingConfig.class));

            // If no banner is printed and we have a version, log it instead
            if (!versionPrinted && seedVersion != null) {
                LoggerFactory.getLogger(Seed.class).info("Seed v{}", seedVersion);
            }
        }

        // Proxy
        proxyManager = ProxyManager.get();
        proxyManager.install(configuration.get(ProxyConfig.class));

        // Nuun
        kernelManager = KernelManager.get();

        // Custom initializers
        for (SeedInitializer seedInitializer : ServiceLoader.load(SeedInitializer.class)) {
            try {
                seedInitializer.onInitialization(configuration);
                seedInitializers.add(seedInitializer);
            } catch (Exception e) {
                throw SeedException.wrap(e, CoreErrorCode.ERROR_IN_INITIALIZER)
                        .put("initializerClass", seedInitializer.getClass().getName());
            }
        }

        initialized = true;
    }

    private String getBanner() {
        InputStream bannerStream = ClassLoaders.findMostCompleteClassLoader(Seed.class).getResourceAsStream("banner.txt");
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
}
