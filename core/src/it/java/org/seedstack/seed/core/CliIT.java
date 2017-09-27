/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.Injector;
import com.google.inject.Module;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.seedstack.seed.cli.CliContext;
import org.seedstack.seed.core.fixtures.TestCliCommand;
import org.seedstack.seed.core.rules.SeedITRule;

public class CliIT {
    @Rule
    public SeedITRule rule = new SeedITRule(this, (CliContext) () -> new String[]{"-o", "someValue"});
    private Injector injector;

    @Before
    public void setUp() throws Exception {
        injector = rule.getKernel().objectGraph().as(Injector.class).createChildInjector(
                (Module) binder -> binder.bind(TestCliCommand.class));
    }

    @Test
    public void cliOptionInjection() throws Exception {
        assertThat(injector.getInstance(TestCliCommand.class).getOption()).isEqualTo("someValue");
    }
}
