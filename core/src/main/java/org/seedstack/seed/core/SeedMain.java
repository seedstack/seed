/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.seedstack.shed.exception.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.seedstack.seed.core.internal.ToolLauncher;
import org.seedstack.seed.spi.SeedLauncher;

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
 */
public class SeedMain {
    public static void main(String[] args) {
        String toolName = System.getProperty("tool");
        SeedLauncher seedLauncher;

        if (!Strings.isNullOrEmpty(toolName)) {
            seedLauncher = getToolLauncher(toolName);
        } else {
            seedLauncher = getLauncher();
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    seedLauncher.shutdown();
                    Seed.close();
                } catch (Throwable t) {
                    handleThrowable(t);
                }
            }
        });

        try {
            seedLauncher.launch(args);
        } catch (Throwable t) {
            handleThrowable(t);
            System.exit(-1);
        }
    }

    public static SeedLauncher getLauncher() {
        List<SeedLauncher> entryPointServices = Lists.newArrayList(ServiceLoader.load(SeedLauncher.class));

        if (entryPointServices.size() < 1) {
            throw SeedException.createNew(CoreErrorCode.MISSING_SEED_ENTRY_POINT);
        } else if (entryPointServices.size() > 1) {
            throw SeedException.createNew(CoreErrorCode.MULTIPLE_SEED_ENTRY_POINTS);
        }

        return entryPointServices.get(0);
    }

    public static SeedLauncher getToolLauncher(String toolName) {
        return new ToolLauncher(toolName);
    }

    private static void handleThrowable(Throwable throwable) {
        if (throwable instanceof SeedException) {
            throwable.printStackTrace(System.err);
        } else {
            SeedException.wrap(throwable, CoreErrorCode.UNEXPECTED_EXCEPTION).printStackTrace(System.err);
        }
    }
}
