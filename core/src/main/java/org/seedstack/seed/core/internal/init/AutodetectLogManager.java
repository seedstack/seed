/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.init;

import org.seedstack.seed.LoggingConfig;
import org.seedstack.shed.reflect.Classes;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class AutodetectLogManager implements LogManager {
    private final LogManager logManager;

    private AutodetectLogManager() {
        if (isLogbackInUse()) {
            logManager = new LogbackLogManager();
        } else {
            logManager = new NoOpLogManager();
        }

        try {
            java.util.logging.LogManager.getLogManager().reset();
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
        } catch (Exception e) {
            System.err.println("Unable to install JUL to SLF4J bridge");
        }
    }

    public static LogManager get() {
        return Holder.INSTANCE;
    }

    @Override
    public void configure(LoggingConfig loggingConfig) {
        logManager.configure(loggingConfig);
    }

    @Override
    public void close() {
        SLF4JBridgeHandler.uninstall();
        logManager.close();
    }

    private boolean isLogbackInUse() {
        return isLoggerFactoryActive("ch.qos.logback.classic.LoggerContext");
    }

    private boolean isLoggerFactoryActive(String className) {
        return Classes.optional(className).isPresent() && LoggerFactory.getILoggerFactory().getClass().getName().equals(
                className);
    }

    private static class Holder {
        private static final LogManager INSTANCE = new AutodetectLogManager();
    }
}
