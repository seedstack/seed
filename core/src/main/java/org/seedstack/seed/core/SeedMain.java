/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import com.google.common.base.Strings;
import org.seedstack.seed.spi.SeedLauncher;
import org.seedstack.shed.exception.BaseException;

import java.util.ServiceLoader;

/**
 * <p>
 * Main Seed Java application entry point. It searches classes implementing {@link SeedLauncher} through the
 * {@link ServiceLoader} mechanism. If no class or more than one class is found, it throws an exception. If exactly one
 * class is found, it delegates the Seed application startup to its {@link SeedLauncher#launch(String[])} method.
 * </p>
 * <p>
 * High-level exception handling and diagnostic is done directly in this class.
 * </p>
 */
public class SeedMain {
    /**
     * Entry point of SeedStack non-managed applications (launched from the command-line).
     *
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        String toolName = System.getProperty("seedstack.tool");
        SeedLauncher seedLauncher;

        if (!Strings.isNullOrEmpty(toolName)) {
            seedLauncher = Seed.getToolLauncher(toolName);
        } else {
            seedLauncher = Seed.getLauncher();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                seedLauncher.shutdown();
            } catch (Exception e) {
                handleException(e);
            }
            Seed.close();
        }, "shutdown"));

        try {
            seedLauncher.launch(args);
        } catch (Exception e) {
            handleException(e);
            System.exit(-1);
        }
    }

    private static void handleException(Exception e) {
        BaseException translated = Seed.translateException(e);
        Seed.diagnostic().dumpDiagnosticReport(translated);
        translated.printStackTrace(System.err);
    }
}
