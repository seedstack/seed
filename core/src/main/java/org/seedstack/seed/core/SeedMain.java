/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import com.google.common.collect.Lists;
import org.seedstack.seed.core.api.CoreErrorCode;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.CorePlugin;
import org.seedstack.seed.core.spi.SeedLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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
 *
 * @author adrien.lauer@mpsa.com
 */
public class SeedMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeedMain.class);
    private static final int EXCEPTION_RETURN_CODE = -1;

    public static void main(String[] args) {
        final SeedLauncher seedLauncher = getLauncher();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    seedLauncher.shutdown();
                    LOGGER.info("Seed application stopped");
                } catch (Exception e) {
                    handleException(e);
                    LOGGER.error("Seed application failed to shutdown properly");
                }
            }
        });

        LOGGER.info("Seed application starting with launcher {}", seedLauncher.getClass().getCanonicalName());

        try {
            seedLauncher.launch(args);
        } catch (Exception e) {
            handleException(e);
            LOGGER.error("Seed application halted after exception");
            System.exit(EXCEPTION_RETURN_CODE);
        }
    }

    public static SeedLauncher getLauncher() {
        List<org.seedstack.seed.core.spi.SeedLauncher> entryPointServices = Lists.newArrayList(ServiceLoader.load(SeedLauncher.class));

        if (entryPointServices.size() < 1) {
            throw SeedException.createNew(CoreErrorCode.MISSING_SEED_ENTRY_POINT);
        } else if (entryPointServices.size() > 1) {
            throw SeedException.createNew(CoreErrorCode.MULTIPLE_SEED_ENTRY_POINTS);
        }

        return entryPointServices.get(0);
    }

    private static void handleException(Exception e) {
        LOGGER.error("An exception occurred, collecting diagnostic information");
        CorePlugin.getDiagnosticManager().dumpDiagnosticReport(e);

        if (e instanceof SeedException) {
            e.printStackTrace(System.err);
        } else {
            SeedException.wrap(e, CoreErrorCode.UNEXPECTED_EXCEPTION).printStackTrace(System.err);
        }
    }
}
