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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Config("logs")
public class LogConfig {
    @SingleValue
    @NotNull
    private Level level = Level.INFO;
    private String pattern;
    @Config("loggers")
    private Map<String, LoggerConfig> loggerConfigs = new HashMap<>();

    public Level getLevel() {
        return level;
    }

    public LogConfig setLevel(Level level) {
        this.level = level;
        return this;
    }

    public String getPattern() {
        return pattern;
    }

    public LogConfig setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public Map<String, LoggerConfig> getLoggerConfigs() {
        return Collections.unmodifiableMap(loggerConfigs);
    }

    public LogConfig configureLogger(String loggerName, LoggerConfig loggerConfig) {
        this.loggerConfigs.put(loggerName, loggerConfig);
        return this;
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

    public enum Level {
        ERROR, WARN, INFO, DEBUG, TRACE
    }
}
