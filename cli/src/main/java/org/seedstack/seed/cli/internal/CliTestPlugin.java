/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.cli.internal;

import static org.seedstack.shed.reflect.AnnotationPredicates.atLeastOneMethodAnnotatedWith;
import static org.seedstack.shed.reflect.AnnotationPredicates.elementAnnotatedWith;

import java.util.Map;
import java.util.Optional;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.cli.WithCliCommand;
import org.seedstack.seed.spi.SeedLauncher;
import org.seedstack.seed.testing.LaunchMode;
import org.seedstack.seed.testing.spi.TestContext;
import org.seedstack.seed.testing.spi.TestPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This plugin enables to run SEED command line applications from integration tests. It disables the global SEED
 * kernel start to start its own kernel for each test method.
 */
public class CliTestPlugin implements TestPlugin {
    @Override
    public boolean enabled(TestContext testContext) {
        return atLeastOneMethodAnnotatedWith(WithCliCommand.class, true)
                .or(elementAnnotatedWith(WithCliCommand.class, true)).test(testContext.testClass());
    }

    @Override
    public LaunchMode launchMode(TestContext testContext) {
        return LaunchMode.PER_TEST;
    }

    @Override
    public Optional<? extends SeedLauncher> launcher(TestContext testContext) {
        return Optional.ofNullable(testContext.testMethod()
                .flatMap(WithCliCommandResolver.INSTANCE::apply)
                .orElseGet(() -> WithCliCommandResolver.INSTANCE.apply(testContext.testClass()).orElse(null)))
                .map(CliTestLauncher::new);
    }

    private static class CliTestLauncher extends CliLauncher {
        private static final Logger LOGGER = LoggerFactory.getLogger(CliTestLauncher.class);
        private final WithCliCommand withCliCommand;

        private CliTestLauncher(WithCliCommand withCliCommand) {
            this.withCliCommand = withCliCommand;
        }

        @Override
        public void launch(String[] args, Map<String, String> kernelParameters) throws Exception {
            int statusCode = execute(withCliCommand.command(), new CliContextInternal(args), kernelParameters);
            LOGGER.info("CLI command test finished with status code {}", statusCode);
            if (withCliCommand.expectedStatusCode() != statusCode) {
                throw SeedException.createNew(CliErrorCode.UNEXPECTED_STATUS_CODE)
                        .put("expectedStatusCode", withCliCommand.expectedStatusCode())
                        .put("effectiveStatusCode", statusCode);
            }
        }
    }
}
