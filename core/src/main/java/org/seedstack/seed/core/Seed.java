/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import ch.qos.logback.classic.LoggerContext;
import com.google.common.base.Joiner;
import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.config.KernelConfiguration;
import io.nuun.kernel.core.NuunCore;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.DiagnosticManager;
import org.seedstack.seed.SeedRuntime;
import org.seedstack.seed.core.internal.init.*;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.LogManager;

/**
 * This class is the Seed framework entry point, which is used create and dispose kernels. It handles global
 * initialization and shutdown automatically.
 *
 * @author adrien.lauer@mpsa.com
 */
public class Seed {
    private static final String SEED_PACKAGE_PREFIX = "org.seedstack.seed";

    private static final Configuration bootstrapConfig = new SeedConfigLoader().buildBootstrapConfig();

    private static class Holder {
        private static final Seed INSTANCE = new Seed();
    }

    private final Map<String, DiagnosticManager> diagnosticManagers = new HashMap<String, DiagnosticManager>();
    private int initializationCount = 0;
    private ConsoleManager consoleManager;
    private LogbackManager logbackManager;
    private NuunManager nuunManager;

    private Seed() {
        // no direct instantiation allowed
    }

    public static Kernel createKernel() {
        return createKernel(null, null);
    }

    public static Kernel createKernel(@Nullable Object runtimeContext) {
        return createKernel(runtimeContext, null);
    }

    public static Kernel createKernel(@Nullable Object runtimeContext, @Nullable Map<String, String> parameters) {
        KernelConfiguration kernelConfiguration = NuunCore.newKernelConfiguration();

        if (parameters != null) {
            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                String key = parameter.getKey();
                String value = parameter.getValue();
                kernelConfiguration.param(key, value);
            }
        }

        return createKernel(runtimeContext, kernelConfiguration, true);
    }

    public static Kernel createKernel(@Nullable Object runtimeContext, KernelConfiguration kernelConfiguration, boolean start) {
        synchronized (Holder.INSTANCE) {
            Holder.INSTANCE.init();

            DiagnosticManager diagnosticManager = new DiagnosticManagerImpl();
            SeedRuntime seedRuntime = SeedRuntime.builder()
                    .context(runtimeContext)
                    .diagnosticManager(diagnosticManager)
                    .colorSupported(Holder.INSTANCE.consoleManager.isColorSupported())
                    .version(Seed.class.getPackage().getImplementationVersion())
                    .build();

            // Startup message
            StringBuilder startMessage = new StringBuilder(">>> Starting Seed");
            if (seedRuntime.getVersion() != null) {
                startMessage.append(" v").append(seedRuntime.getVersion());
            }
            LoggerFactory.getLogger(Seed.class).info(startMessage.toString());

            // Banner
            String banner = Holder.INSTANCE.getBanner();
            if (banner != null) {
                System.out.println(banner);
            }

            // Safety checks
            Holder.INSTANCE.checkConsistency(seedRuntime);

            // Kernel
            kernelConfiguration.containerContext(seedRuntime);
            Kernel kernel = Holder.INSTANCE.nuunManager.initKernel(kernelConfiguration);
            if (start) {
                kernel.start();
                LoggerFactory.getLogger(Seed.class).info("Seed started");
            }

            Holder.INSTANCE.registerDiagnosticManager(kernel.name(), diagnosticManager);

            return kernel;
        }
    }

    public static void disposeKernel(Kernel kernel) {
        if (kernel.isStarted()) {
            LoggerFactory.getLogger(Seed.class).info("Stopping Seed");
            kernel.stop();
            LoggerFactory.getLogger(Seed.class).info("<<< Seed stopped");
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

    public static Configuration getConfiguration() {
        return bootstrapConfig;
    }

    synchronized private void init() {
        if (initializationCount == 0) {
            consoleManager = new ConsoleManager();
            consoleManager.install();

            LogManager.getLogManager().reset();
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
            if (isLogbackInUse()) {
                logbackManager = new LogbackManager(bootstrapConfig);
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

    synchronized private DiagnosticManager getDiagnosticManager(String name) {
        DiagnosticManager diagnosticManager = diagnosticManagers.get(name);
        if (diagnosticManager == null) {
            diagnosticManager = new DiagnosticManagerImpl();
        }
        return diagnosticManager;
    }

    synchronized private void registerDiagnosticManager(String name, DiagnosticManager diagnosticManager) {
        diagnosticManagers.put(name, diagnosticManager);
    }

    synchronized private void unregisterDiagnosticManager(String name) {
        diagnosticManagers.remove(name);
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

    private void checkConsistency(SeedRuntime seedRuntime) {
        Set<String> inconsistentPlugins = new HashSet<String>();
        String seedVersion = seedRuntime.getVersion();

        if (seedVersion != null) {
            for (Plugin plugin : ServiceLoader.load(Plugin.class)) {
                Class<? extends Plugin> pluginClass = plugin.getClass();
                Package pluginPackage = pluginClass.getPackage();

                if (pluginPackage != null && pluginPackage.getName().startsWith(SEED_PACKAGE_PREFIX)) {
                    String pluginVersion = pluginPackage.getImplementationVersion();
                    if (pluginVersion != null && !pluginVersion.equals(seedVersion)) {
                        inconsistentPlugins.add(plugin.name());
                    }
                }
            }
        }

        if (!inconsistentPlugins.isEmpty()) {
            LoggerFactory.getLogger(Seed.class).warn("Version inconsistency detected on plugin(s): " + Joiner.on(", ").join(inconsistentPlugins));
        }
    }

    private boolean isLogbackInUse() {
        return SeedReflectionUtils.isClassPresent("ch.qos.logback.classic.LoggerContext") && LoggerFactory.getILoggerFactory() instanceof LoggerContext;
    }
}
