/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.command;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.seedstack.seed.command.CommandRegistry;
import org.seedstack.seed.command.Command;

import java.util.Map;


class CommandModule extends PrivateModule {
    private final Map<String, CommandDefinition> commandDefinitions;

    CommandModule(Map<String, CommandDefinition> commandDefinitions) {
        this.commandDefinitions = commandDefinitions;
    }

    @Override
    protected void configure() {
        for (Map.Entry<String, CommandDefinition> commandEntry : commandDefinitions.entrySet()) {
            bind(Command.class).annotatedWith(Names.named(commandEntry.getKey())).to(commandEntry.getValue().getCommandActionClass());
        }

        bind(new TypeLiteral<Map<String, CommandDefinition>>() {}).toInstance(commandDefinitions);
        bind(CommandRegistry.class).to(CommandRegistryImpl.class);

        expose(new TypeLiteral<Map<String, CommandDefinition>>() {});
        expose(CommandRegistry.class);
    }
}
