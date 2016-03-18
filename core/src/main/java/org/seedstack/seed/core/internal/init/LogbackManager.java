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
import org.slf4j.LoggerFactory;

public class LogbackManager {
    private final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    public synchronized void configure() {
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

            PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
            logEncoder.setContext(context);
            logEncoder.setPattern("%highlight(%-5level) %yellow(%d{ISO8601}) %magenta(%thread) %cyan(%logger{15}) - %msg%n%red(%rEx)");
            logEncoder.start();

            ConsoleAppender<ILoggingEvent> logConsoleAppender = new ConsoleAppender<ILoggingEvent>();
            logConsoleAppender.setContext(context);
            logConsoleAppender.setTarget("System.out");
            logConsoleAppender.setEncoder(logEncoder);
            logConsoleAppender.start();

            Logger nuunLogger = context.getLogger("io.nuun");
            nuunLogger.setLevel(Level.WARN);

            Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.INFO);
            rootLogger.addAppender(logConsoleAppender);
        }
    }

    public synchronized void close() {
        context.stop();
    }

    private boolean isExplicitlyConfigured() {
        return ConfigurationWatchListUtil.getMainWatchURL(context) != null;
    }
}
