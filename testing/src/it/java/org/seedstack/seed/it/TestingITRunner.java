/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.it;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

public class TestingITRunner extends SeedITRunner {
    public TestingITRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    public void run(RunNotifier notifier) {
        assertThat(SeedITRunnerIT.passedBeforeKernel).isFalse();
        assertThat(SeedITRunnerIT.passedBeforeClass).isFalse();
        assertThat(SeedITRunnerIT.passedAfterClass).isFalse();
        assertThat(SeedITRunnerIT.passedAfterKernel).isFalse();
        super.run(notifier);
        assertThat(SeedITRunnerIT.passedBeforeKernel).isTrue();
        assertThat(SeedITRunnerIT.passedBeforeClass).isTrue();
        assertThat(SeedITRunnerIT.passedAfterClass).isTrue();
        assertThat(SeedITRunnerIT.passedAfterKernel).isTrue();
    }
}
