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
import org.seedstack.seed.DiagnosticManager;
import org.seedstack.seed.LogConfig;
import org.seedstack.seed.ProxyConfig;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.seedstack.seed.core.internal.diagnostic.DiagnosticManagerImpl;
import org.seedstack.seed.core.internal.init.AutodetectLogManager;
import org.seedstack.seed.core.internal.init.BaseConfiguration;
import org.seedstack.seed.core.internal.init.ConsoleManager;
import org.seedstack.seed.core.internal.init.GlobalValidatorFactory;
import org.seedstack.seed.core.internal.init.KernelManager;
import org.seedstack.seed.core.internal.init.ProxyManager;
import org.seedstack.seed.spi.log.LogManager;

import javax.annotation.Nullable;
import javax.validation.ValidatorFactory;

/**
 * This class is the Seed framework entry point, which is used create and dispose kernels.
 * It handles global initialization and cleanup.
 */
public class Seed {
    private static boolean initialized = false;
    private static boolean noLogs = false;
    private final Coffig configuration;
    private final ConsoleManager consoleManager;
    private final LogManager logManager;
    private final ValidatorFactory validatorFactory;
    private final ProxyManager proxyManager;
    private final KernelManager kernelManager;

    private static class Holder {
        private static final Seed INSTANCE = new Seed();
    }

    public static void disableLogs() {
        noLogs = true;
    }

    public static Kernel createKernel() {
        return createKernel(null, null, true);
    }

    public static Kernel createKernel(@Nullable Object runtimeContext, @Nullable KernelConfiguration kernelConfiguration, boolean autoStart) {
        Seed instance = getInstance();
        return instance.kernelManager.createKernel(
                SeedRuntime.builder()
                        .context(runtimeContext)
                        .diagnosticManager(new DiagnosticManagerImpl())
                        .configuration(instance.configuration.fork())
                        .validatorFactory(instance.validatorFactory)
                        .build(),
                kernelConfiguration,
                autoStart);
    }

    public static void disposeKernel(Kernel kernel) {
        KernelManager.get().disposeKernel(kernel);
    }

    public static DiagnosticManager diagnostic() {
        return new DiagnosticManagerImpl();
    }

    public static Coffig baseConfiguration() {
        return BaseConfiguration.get();
    }

    public static void close() {
        if (initialized) {
            Seed instance = getInstance();
            instance.proxyManager.uninstall();
            instance.validatorFactory.close();
            instance.consoleManager.uninstall();
            instance.logManager.close();
            initialized = false;
        }
    }

    private static Seed getInstance() {
        try {
            return Holder.INSTANCE;
        } catch (ExceptionInInitializerError e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                throw SeedException.wrap(cause, CoreErrorCode.UNABLE_TO_INITIALIZE_SEED);
            } else {
                throw SeedException.createNew(CoreErrorCode.UNABLE_TO_INITIALIZE_SEED);
            }
        }
    }

    private Seed() {
        // Logging initialization (should silence logs until logging activation later in the initialization)
        logManager = AutodetectLogManager.get();

        // Console
        consoleManager = ConsoleManager.get();
        consoleManager.install();

        // Configuration
        validatorFactory = GlobalValidatorFactory.get();
        configuration = BaseConfiguration.get();

        // Logging activation
        if (!noLogs) {
            logManager.configure(configuration.get(LogConfig.class));
        }

        // Proxy
        proxyManager = ProxyManager.get();
        proxyManager.install(configuration.get(ProxyConfig.class));

        // Nuun
        kernelManager = KernelManager.get();

        initialized = true;
    }
}
