/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
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
import org.seedstack.seed.validation.NotBlank;

@Config("logging")
public class LoggingConfig {
    @SingleValue
    @NotNull
    private Level level = Level.INFO;
    private Map<String, LoggerConfig> loggers = new HashMap<>();
    private ConsoleConfig console = new ConsoleConfig(true);
    private FileConfig file = new FileConfig(false);

    public Level getLevel() {
        return level;
    }

    public LoggingConfig setLevel(Level level) {
        this.level = level;
        return this;
    }

    public Map<String, LoggerConfig> loggers() {
        return Collections.unmodifiableMap(loggers);
    }

    public LoggingConfig configureLogger(String loggerName, LoggerConfig loggerConfig) {
        this.loggers.put(loggerName, loggerConfig);
        return this;
    }

    public ConsoleConfig console() {
        return console;
    }

    public FileConfig file() {
        return file;
    }

    public enum Level {
        ERROR, WARN, INFO, DEBUG, TRACE
    }

    public abstract static class OutputConfig {
        private boolean enabled;
        private String pattern;

        public OutputConfig() {
        }

        public OutputConfig(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public OutputConfig setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getPattern() {
            return pattern;
        }

        public OutputConfig setPattern(String pattern) {
            this.pattern = pattern;
            return this;
        }
    }

    @Config("console")
    public static class ConsoleConfig extends OutputConfig {
        private Output output = Output.STDOUT;

        public ConsoleConfig() {
        }

        public ConsoleConfig(boolean enabled) {
            super(enabled);
        }

        public Output getOutput() {
            return output;
        }

        public ConsoleConfig setOutput(Output output) {
            this.output = output;
            return this;
        }

        public enum Output {
            STDOUT, STDERR
        }
    }

    @Config("file")
    public static class FileConfig extends OutputConfig {
        public static final String DEFAULT_LOG_FILE = "application.log";
        @NotBlank
        private String path = DEFAULT_LOG_FILE;
        private String maxSize;

        public FileConfig() {
        }

        public FileConfig(boolean enabled) {
            super(enabled);
        }

        public String getPath() {
            return path;
        }

        public FileConfig setPath(String path) {
            this.path = path;
            return this;
        }

        public String getMaxSize() {
            return maxSize;
        }

        public FileConfig setMaxSize(String maxSize) {
            this.maxSize = maxSize;
            return this;
        }
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
