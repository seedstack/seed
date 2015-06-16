/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli.rule;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.seedstack.seed.cli.SeedRunner;
import org.seedstack.seed.cli.api.WithCommandLine;
import org.seedstack.seed.cli.spi.CliErrorCode;
import org.seedstack.seed.cli.spi.CommandLineHandler;
import org.seedstack.seed.core.api.SeedException;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.seedstack.seed.it.spi.PausableRunBefores;

import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author epo.jemba@ext.mpsa.com
 */
public class CommandLineITRule implements MethodRule {

    @Override
    public Statement apply(final Statement statement, final FrameworkMethod frameworkMethod, final Object target) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                WithCommandLine annotation = frameworkMethod.getAnnotation(WithCommandLine.class);
                if (annotation == null) {
                    annotation = target.getClass().getAnnotation(WithCommandLine.class);
                }

                if (annotation != null) {
                    String[] value = annotation.value();
                    int expectedExitCode = annotation.expectedExitCode();

                    int returnCode = SeedRunner.execute(value, new CommandLineITCallable(target, statement));

                    assertThat(returnCode).as("Exit code returned by SeedRunner").isEqualTo(expectedExitCode);
                }

                if (statement instanceof PausableRunBefores) {
                    ((PausableRunBefores)statement).resume();
                } else {
                    statement.evaluate();
                }
            }
        };
    }

    private static final class CommandLineITCallable implements Callable<Integer> {
        private final Object target;
        private final Statement statement;

        @Inject
        Injector injector;

        private CommandLineITCallable(Object target, Statement statement) {
            this.target = target;
            this.statement = statement;
        }

        @Override
        public Integer call() throws Exception {
            injector.injectMembers(target);

            CommandLineHandler commandLineHandler;
            try {
                commandLineHandler = injector.getInstance(CommandLineHandler.class);
            } catch(Exception e) {
                throw SeedException.wrap(e, CliErrorCode.NO_COMMAND_LINE_HANDLER_FOUND);
            }

            if (statement instanceof PausableRunBefores) {
                try {
                    ((PausableRunBefores)statement).evaluateAndPause();
                } catch (Throwable t) {
                    throw SeedException.wrap(t, CliErrorCode.EXCEPTION_OCCURRED_BEFORE_CLI_TEST);
                }
            }

            return commandLineHandler.call();
        }
    }
}
