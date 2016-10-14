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
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.seedstack.seed.core.internal.diagnostic.DiagnosticManagerImpl;
import org.seedstack.seed.core.internal.init.AutodetectLogManager;
import org.seedstack.seed.core.internal.init.BaseConfiguration;
import org.seedstack.seed.core.internal.init.ConsoleManager;
import org.seedstack.seed.core.internal.init.GlobalValidatorFactory;
import org.seedstack.seed.core.internal.init.KernelManager;
import org.seedstack.seed.core.utils.SeedLoggingUtils;
import org.seedstack.seed.spi.log.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.annotation.Nullable;
import javax.validation.ValidatorFactory;

/**
 * This class is the Seed framework entry point, which is used create and dispose kernels.
 * It handles global initialization and cleanup.
 */
public class Seed {
    private static final Logger LOGGER = LoggerFactory.getLogger(Seed.class);
    private static boolean initialized = false;
    private static boolean noLogs = false;
    private final Coffig configuration;
    private final LogManager logManager;
    private final ValidatorFactory validatorFactory;
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
        getInstance().kernelManager.disposeKernel(kernel);
    }

    public static DiagnosticManager diagnostic() {
        return new DiagnosticManagerImpl();
    }

    public static Coffig baseConfiguration() {
        return BaseConfiguration.get();
    }

    public static void close() {
        if (initialized) {
            getInstance().validatorFactory.close();
            getInstance().logManager.close();
            ConsoleManager.uninstall();
            SLF4JBridgeHandler.uninstall();
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
        try {
            java.util.logging.LogManager.getLogManager().reset();
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
        } catch (Exception e) {
            SeedLoggingUtils.logWarningWithDebugDetails(LOGGER, e, "Unable to redirect JUL loggers to SLF4J");
        }

        ConsoleManager.install();
        logManager = AutodetectLogManager.get();
        validatorFactory = GlobalValidatorFactory.get();
        configuration = BaseConfiguration.get();
        if (!noLogs) {
            logManager.configure(configuration.get(LogConfig.class));
        }
        kernelManager = KernelManager.get();
        initialized = true;
    }
}
