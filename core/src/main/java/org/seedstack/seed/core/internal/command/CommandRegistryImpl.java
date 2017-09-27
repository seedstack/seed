/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.command;

import com.google.common.base.Strings;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.command.Argument;
import org.seedstack.seed.command.Command;
import org.seedstack.seed.command.CommandRegistry;
import org.seedstack.seed.command.Option;

/**
 * Implementation of the {@link CommandRegistry}.
 */
class CommandRegistryImpl implements CommandRegistry {
    @Inject
    private Map<String, CommandDefinition> commandDefinitions;

    @Inject
    private Injector injector;

    @Override
    public Set<String> getCommandList() {
        return commandDefinitions.keySet();
    }

    @Override
    public List<Option> getOptionsInfo(String scope, String name) {
        String qualifiedName = buildQualifiedName(scope, name);

        // Lookup for command definition
        CommandDefinition commandDefinitionDefinition = commandDefinitions.get(qualifiedName);
        if (commandDefinitionDefinition == null) {
            throw SeedException.createNew(CommandErrorCode.COMMAND_DEFINITION_NOT_FOUND).put("command", qualifiedName);
        }

        List<Option> options = new ArrayList<>();
        for (OptionDefinition optionDefinition : commandDefinitionDefinition.getOptionDefinitions()) {
            options.add(optionDefinition.getAnnotation());
        }

        return options;
    }

    @Override
    public List<Argument> getArgumentsInfo(String scope, String name) {
        String qualifiedName = buildQualifiedName(scope, name);

        // Lookup for command definition
        CommandDefinition commandDefinitionDefinition = commandDefinitions.get(qualifiedName);
        if (commandDefinitionDefinition == null) {
            throw SeedException.createNew(CommandErrorCode.COMMAND_DEFINITION_NOT_FOUND).put("command", qualifiedName);
        }

        List<Argument> arguments = new ArrayList<>();
        for (ArgumentDefinition argumentDefinition : commandDefinitionDefinition.getArgumentDefinitions()) {
            arguments.add(argumentDefinition.getAnnotation());
        }

        return arguments;
    }

    @Override
    public org.seedstack.seed.command.CommandDefinition getCommandInfo(String scope, String name) {
        String qualifiedName = buildQualifiedName(scope, name);

        // Lookup for command definition
        CommandDefinition commandDefinitionDefinition = commandDefinitions.get(qualifiedName);
        if (commandDefinitionDefinition == null) {
            throw SeedException.createNew(CommandErrorCode.COMMAND_DEFINITION_NOT_FOUND).put("command", qualifiedName);
        }

        return commandDefinitionDefinition.getAnnotation();
    }

    @Override
    public Command createCommand(String scope, String name, List<String> args, Map<String, String> options) {
        String qualifiedName = buildQualifiedName(scope, name);

        // Lookup for command definition
        CommandDefinition commandDefinitionDefinition = commandDefinitions.get(qualifiedName);
        if (commandDefinitionDefinition == null) {
            throw SeedException.createNew(CommandErrorCode.COMMAND_DEFINITION_NOT_FOUND).put("command", qualifiedName);
        }

        // Retrieve the command instance
        Command command;
        try {
            command = injector.getInstance(Key.get(Command.class, Names.named(qualifiedName)));
        } catch (Exception e) {
            throw SeedException.wrap(e, CommandErrorCode.UNABLE_TO_INSTANTIATE_COMMAND).put("command", qualifiedName);
        }

        // Check the number of arguments
        if (args.size() > commandDefinitionDefinition.getArgumentDefinitions().size()) {
            throw SeedException.createNew(CommandErrorCode.TOO_MANY_ARGUMENTS).put("command", qualifiedName).put(
                    "accepted", commandDefinitionDefinition.getArgumentDefinitions().size()).put("given", args.size());
        }

        // Set command arguments
        int i = 0;
        for (ArgumentDefinition argumentDefinition : commandDefinitionDefinition.getArgumentDefinitions()) {
            String value = null;

            if (i >= args.size()) {
                if (argumentDefinition.isMandatory()) {
                    throw SeedException.createNew(CommandErrorCode.MISSING_ARGUMENTS).put("command", qualifiedName).put(
                            "required", i + 1).put("given", args.size());
                } else if (!Strings.isNullOrEmpty(argumentDefinition.getDefaultValue())) {
                    value = argumentDefinition.getDefaultValue();
                }
            } else {
                value = args.get(i);
            }

            if (value != null) {
                try {
                    argumentDefinition.getField().set(command, value);
                } catch (IllegalAccessException e) {
                    throw SeedException.wrap(e, CommandErrorCode.UNABLE_TO_INJECT_ARGUMENT).put("command",
                            qualifiedName).put("argument", argumentDefinition.getName());
                }
            }

            i++;
        }

        // Set command options
        for (OptionDefinition optionDefinition : commandDefinitionDefinition.getOptionDefinitions()) {
            String value = null;

            if (optionDefinition.hasArgument()) {
                if (options.containsKey(optionDefinition.getName())) {
                    value = options.get(optionDefinition.getName());
                } else {
                    if (optionDefinition.isMandatory()) {
                        if (Strings.isNullOrEmpty(optionDefinition.getDefaultValue())) {
                            throw SeedException.createNew(CommandErrorCode.MISSING_MANDATORY_OPTION).put("command",
                                    qualifiedName).put("option", optionDefinition.getName());
                        } else {
                            value = optionDefinition.getDefaultValue();
                        }
                    } else if (!Strings.isNullOrEmpty(optionDefinition.getDefaultValue())) {
                        value = optionDefinition.getDefaultValue();
                    }
                }

                if (value != null) {
                    try {
                        optionDefinition.getField().set(command, value);
                    } catch (IllegalAccessException e) {
                        throw SeedException.wrap(e, CommandErrorCode.UNABLE_TO_INJECT_OPTION).put("command",
                                qualifiedName).put("option", optionDefinition.getName());
                    }
                }
            } else {
                try {
                    optionDefinition.getField().set(command, options.containsKey(optionDefinition.getName()));
                } catch (IllegalAccessException e) {
                    throw SeedException.wrap(e, CommandErrorCode.UNABLE_TO_INJECT_OPTION).put("command",
                            qualifiedName).put("option", optionDefinition.getName());
                }
            }
        }

        return command;
    }

    private String buildQualifiedName(String scope, String name) {
        return (Strings.isNullOrEmpty(scope) ? "" : scope + ":") + name;
    }
}
