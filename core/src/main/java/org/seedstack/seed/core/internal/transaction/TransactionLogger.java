/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TransactionLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionLogger.class);
    private final String prefix;
    private final boolean isTraceEnabled;

    TransactionLogger() {
        this.prefix = String.format("TX[%d]: ", Thread.currentThread().getId());
        this.isTraceEnabled = LOGGER.isTraceEnabled();
    }

    public void log(String format, Object... arguments) {
        if (isTraceEnabled) {
            LOGGER.trace(prefix + format, arguments);
        }
    }
}
