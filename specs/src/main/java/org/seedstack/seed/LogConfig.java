/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;

import javax.validation.constraints.NotNull;

@Config("logs")
public class LogConfig {
    @SingleValue
    @NotNull
    private Level level = Level.INFO;

    public Level getLevel() {
        return level;
    }

    public enum Level {
        ERROR, WARN, INFO, DEBUG, TRACE
    }
}
