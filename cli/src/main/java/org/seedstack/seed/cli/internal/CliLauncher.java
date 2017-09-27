/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
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
import java.util.Arrays;
import java.util.concurrent.Callable;
import javax.inject.Inject;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.cli.CliConfig;
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

    /**
     * Execute a Seed CLI command (implemented by a {@link CommandLineHandler}.
     *
     * @param args the command line arguments. First argument is the name of the CLI command. Subsequent arguments are
     *             passed to the CLI command.
     * @return the return code of the CLI command.
     * @throws Exception when the CLI command fails to complete.
     */
    public static int execute(String[] args) throws Exception {
        CliConfig cliConfig = Seed.baseConfiguration().get(CliConfig.class);
        Callable<Integer> callable;
        String[] effectiveArgs;

        if (cliConfig.hasDefaultCommand()) {
            LOGGER.debug("Executing default CLI command " + cliConfig.getDefaultCommand());
            callable = new SeedCallable(cliConfig.getDefaultCommand());
            effectiveArgs = args;
        } else {
            if (args == null || args.length == 0 || args[0].isEmpty()) {
                throw SeedException.createNew(CliErrorCode.NO_COMMAND_SPECIFIED);
            }
            callable = new SeedCallable(args[0]);
            effectiveArgs = Arrays.copyOfRange(args, 1, args.length);
        }

        Kernel kernel = null;
        try {
            kernel = Seed.createKernel(new CliContextInternal(effectiveArgs), null, true);
            kernel.objectGraph().as(Injector.class).injectMembers(callable);
            return callable.call();
        } finally {
            Seed.disposeKernel(kernel);
        }
    }

    /**
     * Method to execute a callable as a CLI application.
     *
     * @param args     the command line arguments.
     * @param callable the callable to execute
     * @return the return code of the callable
     * @throws Exception when the CLI command fails to complete.
     */
    public static int execute(String[] args, Callable<Integer> callable) throws Exception {
        Kernel kernel = Seed.createKernel(new CliContextInternal(args), null, true);

        try {
            kernel.objectGraph().as(Injector.class).injectMembers(callable);
            return callable.call();
        } finally {
            Seed.disposeKernel(kernel);
        }
    }

    /**
     * Method to execute a callable as a CLI application.
     *
     * @param args                the command line arguments.
     * @param callable            the callable to execute
     * @param kernelConfiguration a kernel configuration to use for the CLI application.
     * @return the return code of the callable
     * @throws Exception when the CLI command fails to complete.
     */
    public static int execute(String[] args, Callable<Integer> callable,
            KernelConfiguration kernelConfiguration) throws Exception {
        Kernel kernel = Seed.createKernel(new CliContextInternal(args), kernelConfiguration, true);

        try {
            kernel.objectGraph().as(Injector.class).injectMembers(callable);
            return callable.call();
        } finally {
            Seed.disposeKernel(kernel);
        }
    }

    @Override
    @SuppressFBWarnings(value = "DM_EXIT", justification = "CliLauncher must be able to return a code to the system")
    public void launch(String[] args) throws Exception {
        int returnCode = execute(args);
        LOGGER.info("CLI command finished with return code {}", returnCode);
        System.exit(returnCode);
    }

    @Override
    public void shutdown() throws Exception {
        // nothing to do here
    }

    public static class SeedCallable implements Callable<Integer> {
        private final String cliCommand;
        @Inject
        private Injector injector;

        SeedCallable(String cliCommand) {
            this.cliCommand = cliCommand;
        }

        @Override
        public Integer call() throws Exception {
            try {
                CommandLineHandler commandLineHandler = injector.getInstance(
                        Key.get(CommandLineHandler.class, Names.named(cliCommand)));
                LOGGER.info("Executing CLI command {}, handled by {}", cliCommand,
                        commandLineHandler.getClass().getCanonicalName());
                return commandLineHandler.call();
            } catch (ConfigurationException e) {
                throw SeedException.wrap(e, CliErrorCode.COMMAND_LINE_HANDLER_NOT_FOUND).put("commandLineHandler",
                        cliCommand);
            }
        }
    }
}
