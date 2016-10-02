/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.logging;

import org.seedstack.seed.LogConfig;
import org.seedstack.seed.spi.log.LogManager;

public class NoOpLogManager implements LogManager {
    @Override
    public void init(LogConfig logConfig) {
        // nothing to do
    }

    @Override
    public void close() {
        // nothing to do
    }
}
