/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import static com.google.common.base.Preconditions.checkState;
import static org.seedstack.shed.misc.PriorityUtils.sortByPriority;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.config.KernelConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.ServiceLoader;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiRenderer;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.ApplicationConfig;
import org.seedstack.seed.LoggingConfig;
import org.seedstack.seed.ProxyConfig;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.seedstack.seed.core.internal.ToolLauncher;
import org.seedstack.seed.core.internal.diagnostic.DiagnosticManagerImpl;
import org.seedstack.seed.core.internal.init.AutodetectLogManager;
import org.seedstack.seed.core.internal.init.BaseConfigurationFactory;
import org.seedstack.seed.core.internal.init.ConsoleManager;
import org.seedstack.seed.core.internal.init.KernelManager;
import org.seedstack.seed.core.internal.init.LogManager;
import org.seedstack.seed.core.internal.init.ProxyManager;
import org.seedstack.seed.diagnostic.DiagnosticManager;
import org.seedstack.seed.spi.SeedExceptionTranslator;
import org.seedstack.seed.spi.SeedInitializer;
import org.seedstack.seed.spi.SeedLauncher;
import org.seedstack.shed.ClassLoaders;
import org.seedstack.shed.exception.BaseException;
import org.seedstack.shed.misc.PriorityUtils;
import org.seedstack.shed.reflect.Classes;
import org.seedstack.shed.text.TextTemplate;

/**
 * This class is the SeedStack framework entry point, which is used create and dispose kernels.
 * It handles global initialization and cleanup.
 */
public class Seed {
    private static final String WELCOME_MESSAGE = "\n" +
            " ____                _ ____  _             _    \n" +
            "/ ___|  ___  ___  __| / ___|| |_ __ _  ___| | __\n" +
            "\\___ \\ / _ \\/ _ \\/ _  \\___ \\| __/ _  |/ __| |/ /\n" +
            " ___) |  __/  __/ (_| |___) | || (_| | (__|   < \n" +
            "|____/ \\___|\\___|\\____|____/ \\__\\____|\\___|_|\\_\\";
    private static final List<SeedInitializer> seedInitializers;
    private static final List<SeedExceptionTranslator> exceptionTranslators;
    private static final DiagnosticManager diagnosticManager;
    private static final String seedVersion;
    private static final String businessVersion;
    private static volatile boolean initialized = false;
    private static volatile boolean disposed = false;
    private static volatile boolean noLogs = false;
    private static volatile boolean hasLifecycleExceptionHandler = false;

    static {
        diagnosticManager = new DiagnosticManagerImpl();

        seedInitializers = Lists.newArrayList(ServiceLoader.load(SeedInitializer.class));
        sortByPriority(seedInitializers, PriorityUtils::priorityOfClassOf);

        exceptionTranslators = Lists.newArrayList(ServiceLoader.load(SeedExceptionTranslator.class));
        sortByPriority(exceptionTranslators, PriorityUtils::priorityOfClassOf);

        seedVersion = Optional.ofNullable(Seed.class.getPackage())
                .map(Package::getImplementationVersion)
                .orElse(null);

        businessVersion = Classes.optional("org.seedstack.business.internal.BusinessSpecifications")
                .map(Class::getPackage)
                .map(Package::getImplementationVersion)
                .orElse(null);
    }

    private final BaseConfigurationFactory baseConfigurationFactory;
    private final Coffig configuration;
    private final ConsoleManager consoleManager;
    private final LogManager logManager;
    private final ProxyManager proxyManager;
    private final KernelManager kernelManager;

    private Seed() {
        // Trigger beforeInitialization() in custom initializers
        for (SeedInitializer seedInitializer : seedInitializers) {
            try {
                seedInitializer.beforeInitialization();
            } catch (Exception e) {
                throw SeedException.wrap(e, CoreErrorCode.ERROR_IN_INITIALIZER)
                        .put("initializerClass", seedInitializer.getClass().getName());
            }
        }

        // Setup a default exception handler that translates exceptions
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Throwable translated;
            if (throwable instanceof Exception) {
                translated = Seed.translateException((Exception) throwable);
            } else {
                translated = throwable;
            }
            diagnosticManager.dumpDiagnosticReport(throwable);
            translated.printStackTrace(System.err);
        });

        // Initialize logging subsystem (should silence logs until logging activation later in the initialization)
        logManager = AutodetectLogManager.get();

        // Create base configuration
        baseConfigurationFactory = BaseConfigurationFactory.get();
        configuration = baseConfigurationFactory.create();

        // Trigger onInitialization() in custom initializers
        for (SeedInitializer seedInitializer : seedInitializers) {
            try {
                seedInitializer.onInitialization(configuration);
            } catch (Exception e) {
                throw SeedException.wrap(e, CoreErrorCode.ERROR_IN_INITIALIZER)
                        .put("initializerClass", seedInitializer.getClass().getName());
            }
        }

        // Access application configuration
        ApplicationConfig applicationConfig = configuration.get(ApplicationConfig.class);

        // Install console enhancements
        consoleManager = ConsoleManager.get();
        consoleManager.install(applicationConfig.getColorOutput());

        // Print banner
        if (!noLogs && applicationConfig.isPrintBanner()) {
            System.out.println(buildBannerMessage(applicationConfig).orElseGet(this::buildWelcomeMessage));
        }

        // Logging activation
        if (!noLogs) {
            logManager.configure(configuration.get(LoggingConfig.class));
        }

        // Install global proxy handling
        proxyManager = ProxyManager.get();
        proxyManager.install(configuration.get(ProxyConfig.class));

        // Create kernel manager
        kernelManager = KernelManager.get();

        // Trigger afterInitialization() in custom initializers
        for (SeedInitializer seedInitializer : seedInitializers) {
            try {
                seedInitializer.afterInitialization();
            } catch (Exception e) {
                throw SeedException.wrap(e, CoreErrorCode.ERROR_IN_INITIALIZER)
                        .put("initializerClass", seedInitializer.getClass().getName());
            }
        }

        markInitialized();
    }

    static void markLifecycleExceptionHandlerEnabled() {
        hasLifecycleExceptionHandler = true;
    }

    /**
     * Returns if a global exception handler for startup and shutdown is present or not.
     *
     * @return true if a global exception handler is catching all startup/shutdown exceptions, false otherwise.
     */
    public static boolean hasLifecycleExceptionHandler() {
        return hasLifecycleExceptionHandler;
    }

    /**
     * Disable logs globally if a supported SLF4J implementation is used. Currently only Logback is supported.
     */
    public static void disableLogs() {
        noLogs = true;
    }

    /**
     * Discover implementations of {@link SeedLauncher} through the {@link ServiceLoader} mechanism and if exactly one
     * implementation is available, returns it. Otherwise, throws an exception.
     *
     * @return an instance of the unique {@link SeedLauncher} implementation.
     */
    public static SeedLauncher getLauncher() {
        List<SeedLauncher> entryPointServices = Lists.newArrayList(ServiceLoader.load(SeedLauncher.class));

        if (entryPointServices.size() < 1) {
            throw SeedException.createNew(CoreErrorCode.MISSING_SEEDSTACK_LAUNCHER);
        } else if (entryPointServices.size() > 1) {
            throw SeedException.createNew(CoreErrorCode.MULTIPLE_SEEDSTACK_LAUNCHERS);
        }

        return entryPointServices.get(0);
    }

    /**
     * Returns an instance of the {@link ToolLauncher} configured for the specified tool.
     *
     * @param toolName the tool to execute.
     * @return the {@link ToolLauncher} instance.
     */
    public static SeedLauncher getToolLauncher(String toolName) {
        return new ToolLauncher(toolName);
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
     * Translates any exception that occurred in the application using an extensible exception mechanism.
     *
     * @param exception the exception to handle.
     * @return the translated exception.
     */
    public static BaseException translateException(Exception exception) {
        if (exception instanceof BaseException) {
            return (BaseException) exception;
        } else {
            for (SeedExceptionTranslator exceptionTranslator : exceptionTranslators) {
                if (exceptionTranslator.canTranslate(exception)) {
                    return exceptionTranslator.translate(exception);
                }
            }
            return SeedException.wrap(exception, CoreErrorCode.UNEXPECTED_EXCEPTION);
        }
    }

    /**
     * Provides the application base configuration (i.e. not including configuration sources discovered after kernel
     * startup).
     * <p>
     * <p>Cannot be called from {@link SeedInitializer} methods.</p>
     *
     * @return the {@link Coffig} object for application base configuration.
     */
    public static Coffig baseConfiguration() {
        return getInstance().configuration;
    }

    /**
     * Create and start a basic kernel without specifying a runtime context, nor a configuration. SeedStack JVM-global
     * state is automatically initialized before the first time a kernel is created.
     * <p>
     * <p>Cannot be called from {@link SeedInitializer} methods.</p>
     *
     * @return the {@link Kernel} instance.
     */
    public static Kernel createKernel() {
        return createKernel(null, null, true);
    }

    /**
     * Create, initialize and optionally start a kernel with the specified runtime context and configuration. Seed
     * JVM-global
     * state is automatically initialized before the first time a kernel is created.
     * <p>
     * <p>Cannot be called from {@link SeedInitializer} methods.</p>
     *
     * @param runtimeContext      the runtime context object, which will be accessible from plugins.
     * @param kernelConfiguration the kernel configuration.
     * @param autoStart           if true, the kernel is started automatically.
     * @return the {@link Kernel} instance.
     */
    public static Kernel createKernel(Object runtimeContext, KernelConfiguration kernelConfiguration,
            boolean autoStart) {
        Seed instance = getInstance();
        return instance.kernelManager.createKernel(
                SeedRuntime.builder()
                        .context(runtimeContext)
                        .diagnosticManager(diagnosticManager)
                        .configuration(instance.configuration.fork())
                        .version(seedVersion)
                        .businessVersion(businessVersion)
                        .build(),
                kernelConfiguration,
                autoStart);
    }

    /**
     * Stops and dispose a running {@link Kernel} instance.
     * <p>
     * <p>Cannot be called from {@link SeedInitializer} methods.</p>
     *
     * @param kernel the kernel to dispose.
     */
    public static void disposeKernel(Kernel kernel) {
        getInstance().kernelManager.disposeKernel(kernel);
    }

    /**
     * Explicitly cleanup SeedStack global state. After calling this method, SeedStack is no longer usable in the
     * current classloader and cannot be reinitialized. Only call this method in standalone JVM environments, just
     * before exiting, typically in a shutdown hook.
     * <p>
     * <p>Has no effect if called from {@link SeedInitializer} methods or after the first call.</p>
     */
    public static void close() {
        if (initialized && !disposed) {
            getInstance().dispose();
        }
    }

    public static void refresh() {
        if (initialized && !disposed) {
            Seed instance = getInstance();
            instance.configuration.refresh();
            instance.logManager.refresh(instance.configuration.get(LoggingConfig.class));
            instance.proxyManager.refresh(instance.configuration.get(ProxyConfig.class));

            // Trigger afterRefresh() in custom initializers
            for (SeedInitializer seedInitializer : seedInitializers) {
                try {
                    seedInitializer.afterRefresh();
                } catch (Exception e) {
                    throw SeedException.wrap(e, CoreErrorCode.ERROR_IN_INITIALIZER)
                            .put("initializerClass", seedInitializer.getClass().getName());
                }
            }
        }
    }

    private static Seed getInstance() {
        checkState(!disposed, "SeedStack cannot be used during or after shutdown");
        try {
            Seed instance = Holder.INSTANCE;
            checkState(initialized, "SeedStack cannot be used before or during initialization");
            return instance;
        } catch (Throwable t) {
            if (t instanceof ExceptionInInitializerError) {
                t = t.getCause();
            }
            throw SeedException.wrap(t, CoreErrorCode.UNABLE_TO_INITIALIZE_SEEDSTACK);
        }
    }

    private static void markInitialized() {
        initialized = true;
    }

    private static void markDisposed() {
        initialized = false;
        disposed = true;
    }

    private Optional<String> buildBannerMessage(ApplicationConfig applicationConfig) {
        String banner = getBanner();
        if (banner != null) {
            Map<String, Object> bannerReplacements = new HashMap<>();
            bannerReplacements.put("seed.version", seedVersion);
            bannerReplacements.put("business.version", businessVersion);
            bannerReplacements.put("app.id", applicationConfig.getId());
            bannerReplacements.put("app.name", applicationConfig.getName());
            bannerReplacements.put("app.version", applicationConfig.getVersion());
            return Optional.of(AnsiRenderer.render(new TextTemplate(banner).render(bannerReplacements)));
        } else {
            return Optional.empty();
        }
    }

    private String buildWelcomeMessage() {
        Ansi welcomeMessage = Ansi.ansi().reset().fgBrightGreen().a(WELCOME_MESSAGE).reset();
        if (seedVersion != null) {
            welcomeMessage.a("\n").a("Core v").a(Strings.padEnd(seedVersion, 16, ' '));
        }
        if (businessVersion != null) {
            welcomeMessage.a(seedVersion != null ? "" : "\n").a("Business v").a(businessVersion);
        }
        welcomeMessage.a("\n");
        return welcomeMessage.reset().toString();
    }

    private String getBanner() {
        InputStream bannerStream = ClassLoaders.findMostCompleteClassLoader(Seed.class).getResourceAsStream(
                "banner.txt");
        if (bannerStream != null) {
            try {
                return new Scanner(bannerStream, StandardCharsets.UTF_8.name()).useDelimiter("\\Z").next();
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

    private void dispose() {
        markDisposed();

        // Trigger onClose() in custom initializers
        for (SeedInitializer seedInitializer : seedInitializers) {
            try {
                seedInitializer.onClose();
            } catch (Exception e) {
                throw SeedException.wrap(e, CoreErrorCode.ERROR_IN_INITIALIZER)
                        .put("initializerClass", seedInitializer.getClass().getName());
            }
        }

        // Uninstall global proxy handling
        proxyManager.uninstall();

        // Uninstall console enhancements
        consoleManager.uninstall();

        // Close base configuration factory
        baseConfigurationFactory.close();

        // Close logging subsystem
        logManager.close();

        // Remove default uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(null);
    }

    private static class Holder {
        private static final Seed INSTANCE = new Seed();
    }
}
