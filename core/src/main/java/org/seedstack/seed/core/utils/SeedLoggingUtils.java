/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;


import org.slf4j.Logger;

/**
 * Seed logging utilities.
 */
public final class SeedLoggingUtils {
    private SeedLoggingUtils() {
        // no instantiation allowed
    }

    /**
     * Log a warning message with the corresponding throwable being logged at debug level.
     *
     * @param logger  the logged used to log the message.
     * @param t       the corresponding throwable.
     * @param message the message.
     * @param values  the values associated with the message.
     */
    public static void logWarningWithDebugDetails(Logger logger, Throwable t, String message, Object... values) {
        logger.warn(String.format("%s (details at debug level)", message), values);
        logger.debug("Stacktrace of the preceding warning", t);
    }

}
