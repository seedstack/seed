/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.init;

import static com.google.common.base.Strings.isNullOrEmpty;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.util.FileSize;
import java.util.Map;
import org.seedstack.seed.LoggingConfig;
import org.seedstack.shed.reflect.Classes;
import org.slf4j.LoggerFactory;

class LogbackLogManager implements LogManager {
    private static final String DEFAULT_CONSOLE_PATTERN =
            "%highlight(%-5level) %d{ISO8601} %magenta(%-15thread) %cyan(%-40logger{40}) %msg%n%red(%throwable)";
    private static final String DEFAULT_FILE_PATTERN =
            "%-5level %d{ISO8601} %-15thread %-40logger{40} %msg%n%throwable";
    private final boolean underTomcat = Classes.optional("org.apache.catalina.startup.Catalina").isPresent();
    private final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    LogbackLogManager() {
        context.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.INFO);
    }

    @Override
    public synchronized void configure(LoggingConfig loggingConfig) {
        if (context.isStarted()) {
            context.stop();
        }
        context.reset();
        context.start();

        boolean autoConfigurationFailed = false;
        try {
            new ContextInitializer(context).autoConfig();
        } catch (JoranException e) {
            autoConfigurationFailed = true;
        }

        if (autoConfigurationFailed || !isExplicitlyConfigured()) {
            context.reset();

            // Root logger level
            Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(convertLevel(loggingConfig.getLevel()));

            // Configure
            configureConsole(loggingConfig.console(), rootLogger);
            configureFile(loggingConfig.file(), rootLogger);

            // Nuun is quite verbose so it is set to WARN by default
            Logger nuunLogger = context.getLogger("io.nuun");
            nuunLogger.setLevel(Level.WARN);

            // When running under Tomcat with a LevelChangePropagator, DEBUG level and below lead to an exception
            // so we force INFO level
            if (underTomcat && (loggingConfig.getLevel() == LoggingConfig.Level.DEBUG || loggingConfig.getLevel() ==
                    LoggingConfig.Level.TRACE)) {
                context.getLogger("org.apache.catalina").setLevel(Level.INFO);
                context.getLogger("org.apache.juli").setLevel(Level.INFO);
            }

            // Configure explicit loggers
            for (Map.Entry<String, LoggingConfig.LoggerConfig> loggerLevelEntry : loggingConfig.loggers()
                    .entrySet()) {
                Logger logger = context.getLogger(loggerLevelEntry.getKey());
                LoggingConfig.LoggerConfig config = loggerLevelEntry.getValue();
                logger.setLevel(convertLevel(config.getLevel()));
                logger.setAdditive(config.isAdditive());
            }

            // Add level propagator for performance of JUL
            LevelChangePropagator levelChangePropagator = new LevelChangePropagator();
            levelChangePropagator.setContext(context);
            levelChangePropagator.setResetJUL(true);
            context.addListener(levelChangePropagator);
            levelChangePropagator.start();
        }
    }

    private void configureFile(LoggingConfig.FileConfig fileConfig, Logger rootLogger) {
        if (fileConfig.isEnabled()) {
            FileAppender<ILoggingEvent> appender;
            TimeBasedRollingPolicy rollingPolicy = buildRollingPolicy(fileConfig);
            if (rollingPolicy != null) {
                appender = new RollingFileAppender<>();
                ((RollingFileAppender) appender).setRollingPolicy(rollingPolicy);
            } else {
                appender = new FileAppender<>();
            }
            appender.setContext(context);
            appender.setFile(fileConfig.getPath());
            appender.setEncoder(buildEncoder(fileConfig, DEFAULT_FILE_PATTERN));
            appender.start();
            rootLogger.addAppender(appender);
        }
    }

    private TimeBasedRollingPolicy buildRollingPolicy(LoggingConfig.FileConfig fileConfig) {
        TimeBasedRollingPolicy policy;
        if (fileConfig.getMaxSize() != null) {
            policy = new SizeAndTimeBasedRollingPolicy();
            ((SizeAndTimeBasedRollingPolicy) policy).setMaxFileSize(FileSize.valueOf(fileConfig.getMaxSize()));
        } else if (new FileNamePattern(fileConfig.getPath(), context).getPrimaryDateTokenConverter() != null) {
            policy = new TimeBasedRollingPolicy();
            policy.setFileNamePattern(fileConfig.getPath());
        } else {
            policy = null;
        }
        if (policy != null) {
            policy.start();
        }
        return policy;
    }

    private void configureConsole(LoggingConfig.ConsoleConfig consoleConfig, Logger rootLogger) {
        if (consoleConfig.isEnabled()) {
            ConsoleAppender<ILoggingEvent> logConsoleAppender = new ConsoleAppender<>();
            logConsoleAppender.setContext(context);
            switch (consoleConfig.getOutput()) {
                case STDOUT:
                    logConsoleAppender.setTarget("System.out");
                    break;
                case STDERR:
                    logConsoleAppender.setTarget("System.err");
                    break;
                default:
                    logConsoleAppender.setTarget("System.out");
                    break;
            }
            logConsoleAppender.setEncoder(buildEncoder(consoleConfig, DEFAULT_CONSOLE_PATTERN));
            logConsoleAppender.start();
            rootLogger.addAppender(logConsoleAppender);
        }
    }

    private PatternLayoutEncoder buildEncoder(LoggingConfig.OutputConfig outputConfig, String defaultPattern) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern(isNullOrEmpty(outputConfig.getPattern()) ? defaultPattern : outputConfig.getPattern());
        encoder.setContext(context);
        encoder.start();
        return encoder;
    }

    private Level convertLevel(LoggingConfig.Level level) {
        return Level.valueOf(level.name());
    }

    @Override
    public synchronized void close() {
        context.stop();
    }

    @Override
    public void refresh(LoggingConfig loggingConfig) {
        configure(loggingConfig);
    }

    private boolean isExplicitlyConfigured() {
        return ConfigurationWatchListUtil.getMainWatchURL(context) != null;
    }
}
