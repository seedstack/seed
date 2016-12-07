/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.seedstack.seed.command.CommandRegistry;
import org.seedstack.seed.core.fixtures.TestCommand;
import org.seedstack.seed.core.rules.SeedITRule;
import org.seedstack.seed.command.Argument;
import org.seedstack.seed.command.Command;
import org.seedstack.seed.command.Option;

import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandIT {
    @Rule
    public SeedITRule rule = new SeedITRule(this);
    @Inject
    private CommandRegistry commandRegistry;

    @Test
    public void commandRegistryIsInjectable() throws Exception {
        assertThat(commandRegistry).isNotNull();
    }

    @Test
    public void testCommandInstantiation() throws Exception {
        Command command = commandRegistry.createCommand("core", "test", Lists.newArrayList("arg1"), ImmutableMap.of("o1", "o2=toto"));
        assertThat(command).isInstanceOf(TestCommand.class);
        assertThat(command.execute(null)).isNotNull();
    }

    @Test
    public void testArgumentInfo() throws Exception {
        List<Argument> argumentsInfo = commandRegistry.getArgumentsInfo("core", "test");
        assertThat(argumentsInfo).hasSize(1);
        assertThat(argumentsInfo.get(0).name()).isEqualTo("arg1");
    }

    @Test
    public void testOptionsInfo() throws Exception {
        List<Option> optionsInfo = commandRegistry.getOptionsInfo("core", "test");
        assertThat(optionsInfo).hasSize(2);
        assertThat(optionsInfo.get(0).longName()).isEqualTo("option1");
        assertThat(optionsInfo.get(1).longName()).isEqualTo("option2");
    }
}
