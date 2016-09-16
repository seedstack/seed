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
import org.seedstack.seed.spi.log.LogManager;
import org.slf4j.LoggerFactory;

public class LogbackLogManager implements LogManager {
    private final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    public LogbackLogManager() {
        context.getLogger("org.jboss.logging").setLevel(Level.ERROR);
        context.getLogger("org.hibernate.validator").setLevel(Level.ERROR);
    }

    @Override
    public synchronized void init(LogConfig logConfig) {
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
            encoder.setPattern("%highlight(%-5level) [%d{ISO8601}] %magenta(%-8thread) %cyan(%-30logger{30}) %msg%n%red(%throwable)");
            encoder.setContext(context);
            encoder.start();

            ConsoleAppender<ILoggingEvent> logConsoleAppender = new ConsoleAppender<>();
            logConsoleAppender.setContext(context);
            logConsoleAppender.setTarget("System.out");
            logConsoleAppender.setEncoder(encoder);
            logConsoleAppender.start();

            Logger nuunLogger = context.getLogger("io.nuun");
            nuunLogger.setLevel(Level.WARN);

            Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.valueOf(logConfig.getLevel().name()));
            rootLogger.addAppender(logConsoleAppender);
        }
    }

    @Override
    public synchronized void close() {
        context.stop();
    }

    private boolean isExplicitlyConfigured() {
        return ConfigurationWatchListUtil.getMainWatchURL(context) != null;
    }
}
