/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli;

import com.google.inject.ConfigurationException;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.Message;
import org.seedstack.seed.cli.spi.CliErrorCode;
import org.seedstack.seed.cli.spi.CommandLineHandler;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.CorePlugin;
import io.nuun.plugin.cli.NuunCliService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.Callable;

/**
 * This class executes any {@link org.seedstack.seed.cli.spi.CommandLineHandler} found in the classpath.
 *
 * @author epo.jemba@ext.mpsa.com
 */
public final class SeedRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeedRunner.class);

    private SeedRunner() {
    }

    /**
     * Main method to run a standalone SEED application.
     *
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        int returnCode = -1;
        Throwable throwable = null;

        try {
            returnCode = execute(args, new SeedCallable());
        } catch (ConfigurationException e) {
            throwable = e;

            LOGGER.error("A configuration error occurred during application startup, collecting diagnostic information");
            CorePlugin.getDiagnosticManager().dumpDiagnosticReport(e);

            for (Message message : e.getErrorMessages()) {
                for (Object source : message.getSources()) {
                    if (source.getClass().equals(TypeLiteral.class) && TypeLiteral.class.cast(source).getRawType().equals(SeedCallable.class)) {
                        LOGGER.error("Unable to find a CommandLineHandler in the classpath");
                    }
                }
            }
        } catch (Throwable t) {
            throwable = t;

            LOGGER.error("An unexpected error occurred during application execution, collecting diagnostic information");
            CorePlugin.getDiagnosticManager().dumpDiagnosticReport(t);
        } finally {
            // Dump the throwable to stderr if needed
            if (throwable != null) {
                SeedException.wrap(throwable, CliErrorCode.UNEXPECTED_CLI_ERROR).printStackTrace(System.err); // NOSONAR
            }

            // Exit with the return code
            LOGGER.info("Exiting application with code " + returnCode);
            System.exit(returnCode); // NOSONAR
        }
    }

    /**
     * Method to execute a callable as a CLI application.
     *
     * @param args     the command line arguments.
     * @param callable the callable to execute
     * @return the return code of the callable
     * @throws Exception when something goes wrong.
     */
    public static int execute(String[] args, Callable<Integer> callable) throws Exception {
        NuunCliService nuunCliService = new NuunCliService();
        return nuunCliService.startSync(args, callable);
    }

    private static final class SeedCallable implements Callable<Integer> {
        private static final Logger LOGGER = LoggerFactory.getLogger(SeedCallable.class);

        @Inject
        private CommandLineHandler commandLineHandler;

        private SeedCallable() {
        }

        @Override
        public Integer call() throws Exception {
            LOGGER.info("Starting command-line handler: " + commandLineHandler.name());

            try {
                return commandLineHandler.call();
            } finally {
                LOGGER.info("Ending command-line handler: " + commandLineHandler.name());
            }
        }
    }
}
