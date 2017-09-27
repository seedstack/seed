/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.cli.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.nuun.kernel.api.config.KernelConfiguration;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.cli.WithCommandLine;
import org.seedstack.seed.it.ITBind;
import org.seedstack.seed.it.spi.KernelRule;
import org.seedstack.seed.it.spi.PausableStatement;

@ITBind
public class CommandLineITRule implements MethodRule, KernelRule {
    private KernelConfiguration kernelConfiguration;

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
                    String[] args = annotation.args();
                    int returnCode = CliLauncher.execute(args,
                            new CommandLineITCallable(target, statement, annotation.command()), kernelConfiguration);
                    assertThat(returnCode).as("Exit code returned by SeedRunner").isEqualTo(
                            annotation.expectedExitCode());
                }

                if (statement instanceof PausableStatement) {
                    ((PausableStatement) statement).resume();
                } else {
                    statement.evaluate();
                }
            }
        };
    }

    @Override
    public void acceptKernelConfiguration(KernelConfiguration kernelConfiguration) {
        this.kernelConfiguration = kernelConfiguration;
    }

    private static final class CommandLineITCallable extends CliLauncher.SeedCallable {
        private final Object target;
        private final Statement statement;

        @Inject
        private Injector injector;

        private CommandLineITCallable(Object target, Statement statement, String command) {
            super(command);
            this.target = target;
            this.statement = statement;
        }

        @Override
        public Integer call() throws Exception {
            injector.injectMembers(target);

            if (statement instanceof PausableStatement) {
                try {
                    ((PausableStatement) statement).evaluateAndPause();
                } catch (Throwable t) {
                    throw SeedException.wrap(t, CliErrorCode.EXCEPTION_OCCURRED_BEFORE_CLI_TEST);
                }
            }

            return super.call();
        }
    }
}
