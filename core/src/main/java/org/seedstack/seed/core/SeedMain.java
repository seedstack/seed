/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core;

import com.google.common.base.Strings;
import java.util.ServiceLoader;
import org.seedstack.seed.spi.SeedLauncher;
import org.seedstack.shed.exception.BaseException;

/**
 * <p>
 * Main Seed Java application entry point. It searches classes implementing {@link SeedLauncher} through the
 * {@link ServiceLoader} mechanism. If no class or more than one class is found, it throws an exception. If exactly one
 * class is found, it delegates the Seed application startup to its {@link SeedLauncher#launch(String[])} method.
 * </p>
 * <p>
 * Exception handling and diagnostic during startup and shutdown is done directly in this class. This is materialized
 * by the fact that {@link Seed#hasLifecycleExceptionHandler()} returns true when {@link SeedMain} is used.
 * </p>
 * <p>
 * If an exception occurs during startup or shutdown, it is translated using {@link Seed#translateException(Exception)}
 * and its stack trace is printed on the standard error output. During startup only a diagnostic report is also dumped.
 * </p>
 */
public class SeedMain {
    private static final int EXIT_FAILURE = 1;

    /**
     * Entry point of SeedStack non-managed applications (launched from the command-line).
     *
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        try {
            final SeedLauncher seedLauncher;
            final String toolName = System.getProperty("seedstack.tool");
            if (!Strings.isNullOrEmpty(toolName)) {
                seedLauncher = Seed.getToolLauncher(toolName);
            } else {
                seedLauncher = Seed.getLauncher();
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    seedLauncher.shutdown();
                    Seed.close();
                } catch (Exception e) {
                    handleException(e);
                }
            }, "shutdown"));
            Seed.markLifecycleExceptionHandlerEnabled();

            seedLauncher.launch(args);
        } catch (Exception e) {
            handleException(e);
            System.exit(EXIT_FAILURE);
        }
    }

    private static void handleException(Exception e) {
        BaseException translated = Seed.translateException(e);
        Seed.diagnostic().dumpDiagnosticReport(translated);
        translated.printStackTrace(System.err);
    }
}
