/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;

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
import org.seedstack.seed.LogConfig;
import org.seedstack.shed.reflect.Classes;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

class LogbackLogManager implements LogManager {
    private final boolean underTomcat = Classes.optional("org.apache.catalina.startup.Catalina").isPresent();
    private final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    LogbackLogManager() {
        context.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.OFF);
    }

    @Override
    public synchronized void configure(LogConfig logConfig) {
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

            LevelChangePropagator levelChangePropagator = new LevelChangePropagator();
            levelChangePropagator.setContext(context);
            levelChangePropagator.setResetJUL(true);
            levelChangePropagator.start();
            context.addListener(levelChangePropagator);

            PatternLayoutEncoder encoder = new PatternLayoutEncoder();
            encoder.setPattern(isNullOrEmpty(logConfig.getPattern()) ? "%highlight(%-5level) [%d{ISO8601}] %magenta(%-8thread) %cyan(%-30logger{30}) %msg%n%red(%throwable)" : logConfig.getPattern());
            encoder.setContext(context);
            encoder.start();

            ConsoleAppender<ILoggingEvent> logConsoleAppender = new ConsoleAppender<>();
            logConsoleAppender.setContext(context);
            logConsoleAppender.setTarget("System.out");
            logConsoleAppender.setEncoder(encoder);
            logConsoleAppender.start();

            Logger nuunLogger = context.getLogger("io.nuun");
            nuunLogger.setLevel(Level.WARN);

            if (underTomcat && (logConfig.getLevel() == LogConfig.Level.DEBUG || logConfig.getLevel() == LogConfig.Level.TRACE)) {
                // When running under Tomcat with a LevelChangePropagator, DEBUG level and below lead to an exception so we force INFO level
                context.getLogger("org.apache.catalina").setLevel(Level.INFO);
                context.getLogger("org.apache.juli").setLevel(Level.INFO);
            }

            for (Map.Entry<String, LogConfig.LoggerConfig> loggerLevelEntry : logConfig.getLoggerConfigs().entrySet()) {
                Logger logger = context.getLogger(loggerLevelEntry.getKey());
                LogConfig.LoggerConfig config = loggerLevelEntry.getValue();
                logger.setLevel(convertLevel(config.getLevel()));
                logger.setAdditive(config.isAdditive());
            }

            Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(convertLevel(logConfig.getLevel()));
            rootLogger.addAppender(logConsoleAppender);
        }
    }

    private Level convertLevel(LogConfig.Level level) {
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
