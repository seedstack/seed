/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.command;

import com.google.common.base.Strings;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.command.Argument;
import org.seedstack.seed.command.Command;
import org.seedstack.seed.command.Option;

/**
 * Holds the definition of a command.
 */
class CommandDefinition implements Comparable<CommandDefinition> {
    private final org.seedstack.seed.command.CommandDefinition commandDefinition;
    private final Class<? extends Command> commandActionClass;

    private final List<ArgumentDefinition> argumentDefinitions = new ArrayList<>();
    private final List<OptionDefinition> optionDefinitions = new ArrayList<>();

    CommandDefinition(org.seedstack.seed.command.CommandDefinition commandDefinition,
            Class<? extends Command> commandActionClass) {
        this.commandDefinition = commandDefinition;
        this.commandActionClass = commandActionClass;
    }

    void addOptionField(Option option, Field field) {
        optionDefinitions.add(new OptionDefinition(option, field));
    }

    void addArgumentField(Argument argument, Field field) {
        ArgumentDefinition argumentDefinition = new ArgumentDefinition(argument, field);

        int insertionPoint = Collections.binarySearch(this.argumentDefinitions, argumentDefinition);
        if (insertionPoint < 0) {
            argumentDefinitions.add(-insertionPoint - 1, argumentDefinition);
        } else {
            throw SeedException.createNew(CommandErrorCode.ARGUMENT_INDEX_COLLISION).put("command", getQualifiedName());
        }
    }

    List<OptionDefinition> getOptionDefinitions() {
        return optionDefinitions;
    }

    List<ArgumentDefinition> getArgumentDefinitions() {
        return argumentDefinitions;
    }

    String getDescription() {
        return commandDefinition.description();
    }

    String getQualifiedName() {
        return (Strings.isNullOrEmpty(
                commandDefinition.scope()) ? "" : commandDefinition.scope() + ":") + commandDefinition.name();
    }

    String getScope() {
        return commandDefinition.scope();
    }

    String getName() {
        return commandDefinition.name();
    }

    org.seedstack.seed.command.CommandDefinition getAnnotation() {
        return commandDefinition;
    }

    Class<? extends Command> getCommandActionClass() {
        return commandActionClass;
    }

    @Override
    public int compareTo(CommandDefinition o) {
        return getQualifiedName().compareTo(o.getQualifiedName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return getQualifiedName().equals(((CommandDefinition) o).getQualifiedName());
    }

    @Override
    public int hashCode() {
        return getQualifiedName().hashCode();
    }
}
