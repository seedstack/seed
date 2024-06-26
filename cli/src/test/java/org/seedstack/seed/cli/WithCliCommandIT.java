/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Application;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.testing.Arguments;
import org.seedstack.seed.testing.Expected;
import org.seedstack.seed.testing.junit4.SeedITRunner;

@RunWith(SeedITRunner.class)
public class WithCliCommandIT {
    @Inject
    private Application application;
    @Inject
    private DummyCommandLineHandler dummyCommandLineHandler;

    @Test
    public void withoutCommand() {
        assertThat(application).isNotNull();
        assertThat(dummyCommandLineHandler.called).isFalse();
    }

    @Test
    @WithCliCommand(expectedStatusCode = 255, command = "dummy")
    @Arguments({"arg0", "arg1", "--option=value"})
    public void withCommand() {
        assertThat(application).isNotNull();
        assertThat(dummyCommandLineHandler.called).isTrue();
    }

    @Test
    @WithCliCommand(command = "dummy")
    @Arguments({"arg0", "arg1", "--option=value"})
    @Expected(SeedException.class)
    public void wrongStatusCode() {
        assertThat(application).isNotNull();
        assertThat(dummyCommandLineHandler.called).isTrue();
    }
}
