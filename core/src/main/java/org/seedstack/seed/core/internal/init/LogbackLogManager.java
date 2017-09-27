/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
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
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import java.util.Map;
import org.seedstack.seed.LoggingConfig;
import org.seedstack.shed.reflect.Classes;
import org.slf4j.LoggerFactory;

class LogbackLogManager implements LogManager {
    private final boolean underTomcat = Classes.optional("org.apache.catalina.startup.Catalina").isPresent();
    private final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    LogbackLogManager() {
        context.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.OFF);
    }

    @Override
    public synchronized void configure(LoggingConfig loggingConfig) {
        context.reset();
        if (!context.isStarted()) {
            context.start();
        }

        boolean autoConfigurationFailed = false;
        try {
            new ContextInitializer(context).autoConfig();
        } catch (JoranException e) {
            autoConfigurationFailed = true;
        }

        if (autoConfigurationFailed || !isExplicitlyConfigured()) {
            context.reset();

            PatternLayoutEncoder encoder = new PatternLayoutEncoder();
            encoder.setPattern(isNullOrEmpty(
                    loggingConfig.getPattern()) ? "%highlight(%-5level) %d{ISO8601} %magenta(%-15thread) %cyan"
                    + "(%-40logger{40}) %msg%n%red(%throwable)" : loggingConfig.getPattern());
            encoder.setContext(context);
            encoder.start();

            ConsoleAppender<ILoggingEvent> logConsoleAppender = new ConsoleAppender<>();
            logConsoleAppender.setContext(context);
            logConsoleAppender.setTarget("System.out");
            logConsoleAppender.setEncoder(encoder);
            logConsoleAppender.start();

            Logger nuunLogger = context.getLogger("io.nuun");
            nuunLogger.setLevel(Level.WARN);

            if (underTomcat && (loggingConfig.getLevel() == LoggingConfig.Level.DEBUG || loggingConfig.getLevel() ==
                    LoggingConfig.Level.TRACE)) {
                // When running under Tomcat with a LevelChangePropagator, DEBUG level and below lead to an exception
                // so we force INFO level
                context.getLogger("org.apache.catalina").setLevel(Level.INFO);
                context.getLogger("org.apache.juli").setLevel(Level.INFO);
            }

            for (Map.Entry<String, LoggingConfig.LoggerConfig> loggerLevelEntry : loggingConfig.getLoggerConfigs()
                    .entrySet()) {
                Logger logger = context.getLogger(loggerLevelEntry.getKey());
                LoggingConfig.LoggerConfig config = loggerLevelEntry.getValue();
                logger.setLevel(convertLevel(config.getLevel()));
                logger.setAdditive(config.isAdditive());
            }

            Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(convertLevel(loggingConfig.getLevel()));
            rootLogger.addAppender(logConsoleAppender);

            LevelChangePropagator levelChangePropagator = new LevelChangePropagator();
            levelChangePropagator.setContext(context);
            levelChangePropagator.setResetJUL(true);
            context.addListener(levelChangePropagator);
            levelChangePropagator.start();
        }
    }

    private Level convertLevel(LoggingConfig.Level level) {
        return Level.valueOf(level.name());
    }

    @Override
    public synchronized void close() {
        context.stop();
    }

    private boolean isExplicitlyConfigured() {
        return ConfigurationWatchListUtil.getMainWatchURL(context) != null;
    }
}
