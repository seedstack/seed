/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;

import org.seedstack.seed.LogConfig;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.spi.log.LogManager;
import org.slf4j.LoggerFactory;

public class AutodetectLogManager implements LogManager {
    private final LogManager logManager;

    private static class Holder {
        private static final LogManager INSTANCE = new AutodetectLogManager();
    }

    public static LogManager get() {
        return Holder.INSTANCE;
    }

    private AutodetectLogManager() {
        if (isLogbackInUse()) {
            logManager = new LogbackLogManager();
        } else {
            logManager = new NoOpLogManager();
        }
    }

    @Override
    public void configure(LogConfig logConfig) {
        logManager.configure(logConfig);
    }

    @Override
    public void close() {
        logManager.close();
    }

    private boolean isLogbackInUse() {
        return isLoggerFactoryActive("ch.qos.logback.classic.LoggerContext");
    }

    private boolean isLoggerFactoryActive(String className) {
        return SeedReflectionUtils.isClassPresent(className) && LoggerFactory.getILoggerFactory().getClass().getName().equals(className);
    }
}
