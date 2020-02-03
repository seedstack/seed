/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli.internal;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.config.KernelConfiguration;
import io.nuun.kernel.core.NuunCore;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.cli.CliConfig;
import org.seedstack.seed.cli.CliContext;
import org.seedstack.seed.cli.CommandLineHandler;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.spi.SeedLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class executes {@link CommandLineHandler}s found in the classpath.
 */
public class CliLauncher implements SeedLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(CliLauncher.class);
    private final AtomicBoolean launched = new AtomicBoolean(false);
    private Kernel kernel;

    @Override
    @SuppressFBWarnings(value = "DM_EXIT", justification = "CliLauncher must be able to return a code to the system")
    public void launch(String[] args, Map<String, String> kernelParameters) throws Exception {
        CliConfig cliConfig = Seed.baseConfiguration().get(CliConfig.class);
        String cliCommand;
        CliContext cliContext;

        if (cliConfig.hasDefaultCommand()) {
            cliCommand = cliConfig.getDefaultCommand();
            cliContext = new CliContextInternal(args);
        } else {
            if (args == null || args.length == 0 || args[0].isEmpty()) {
                throw SeedException.createNew(CliErrorCode.NO_COMMAND_SPECIFIED);
            }
            cliCommand = args[0];
            cliContext = new CliContextInternal(args, 1);
        }

        int statusCode = execute(cliCommand, cliContext, kernelParameters);
        LOGGER.info("CLI command finished with status code {}", statusCode);
        System.exit(statusCode);
    }

    @Override
    public void shutdown() throws Exception {
        try {
            Seed.disposeKernel(kernel);
        } finally {
            kernel = null;
        }
    }

    @Override
    public Optional<Kernel> getKernel() {
        return Optional.ofNullable(kernel);
    }

    int execute(String cliCommand, CliContext cliContext, Map<String, String> kernelParameters) throws Exception {
        if (launched.compareAndSet(false, true)) {
            KernelConfiguration kernelConfiguration = NuunCore.newKernelConfiguration();
            for (Map.Entry<String, String> kernelParameter : kernelParameters.entrySet()) {
                kernelConfiguration.param(kernelParameter.getKey(), kernelParameter.getValue());
            }

            kernel = Seed.createKernel(cliContext, kernelConfiguration, true);
            CommandLineHandler cliHandler;
            try {
                cliHandler = kernel.objectGraph()
                        .as(Injector.class)
                        .getInstance(Key.get(CommandLineHandler.class, Names.named(cliCommand)));
                LOGGER.info("Executing CLI command {}, handled by {}",
                        cliCommand,
                        cliHandler.getClass().getCanonicalName());
            } catch (ConfigurationException e) {
                throw SeedException.wrap(e, CliErrorCode.COMMAND_LINE_HANDLER_NOT_FOUND)
                        .put("commandLineHandler", cliCommand);
            }

            return cliHandler.call();
        } else {
            throw SeedException.createNew(CliErrorCode.COMMAND_LINE_HANDLER_ALREADY_RUN);
        }
    }
}
