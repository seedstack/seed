/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;

@Config("logging")
public class LoggingConfig {
    @SingleValue
    @NotNull
    private Level level = Level.INFO;
    private String pattern;
    @Config("loggers")
    private Map<String, LoggerConfig> loggerConfigs = new HashMap<>();

    public Level getLevel() {
        return level;
    }

    public LoggingConfig setLevel(Level level) {
        this.level = level;
        return this;
    }

    public String getPattern() {
        return pattern;
    }

    public LoggingConfig setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public Map<String, LoggerConfig> getLoggerConfigs() {
        return Collections.unmodifiableMap(loggerConfigs);
    }

    public LoggingConfig configureLogger(String loggerName, LoggerConfig loggerConfig) {
        this.loggerConfigs.put(loggerName, loggerConfig);
        return this;
    }

    public enum Level {
        ERROR, WARN, INFO, DEBUG, TRACE
    }

    public static class LoggerConfig {
        @SingleValue
        private Level level = Level.INFO;
        private boolean additive = true;

        public Level getLevel() {
            return level;
        }

        public LoggerConfig setLevel(Level level) {
            this.level = level;
            return this;
        }

        public boolean isAdditive() {
            return additive;
        }

        public LoggerConfig setAdditive(boolean additive) {
            this.additive = additive;
            return this;
        }
    }
}
