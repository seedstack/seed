/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.fixtures;

import org.seedstack.seed.DataManager;
import org.seedstack.seed.spi.command.Argument;
import org.seedstack.seed.spi.command.Command;
import org.seedstack.seed.spi.command.CommandDefinition;
import org.seedstack.seed.spi.command.Option;

import javax.inject.Inject;

@CommandDefinition(scope = "core", name = "test", description = "Test command")
public class TestCommand implements Command<Object> {
    @Option(name = "o1", longName = "option1", description = "Option1")
    private boolean o1;
    @Option(name = "o2", longName = "option2", description = "Option2", hasArgument = true)
    private String o2;
    @Argument(index = 0, name="arg1")
    private String arg1;

    @Inject
    DataManager dataManager;

    @Override
    public Object execute(Object input) {
        return new Object();
    }
}