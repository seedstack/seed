/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.core.fixtures.TestCliCommand;
import org.seedstack.seed.testing.Arguments;
import org.seedstack.seed.testing.junit4.SeedITRunner;

@RunWith(SeedITRunner.class)
@Arguments({"-o", "someValue"})
public class CliIT {
    @Inject
    private Injector injector;

    @Test
    public void cliOptionInjection() {
        assertThat(injector.getInstance(TestCliCommand.class).getOption()).isEqualTo("someValue");
    }
}
