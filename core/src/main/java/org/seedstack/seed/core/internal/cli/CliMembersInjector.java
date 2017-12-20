/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.cli;

import static org.seedstack.shed.reflect.ReflectUtils.makeAccessible;

import com.google.common.base.Joiner;
import com.google.inject.MembersInjector;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.cli.CliContext;
import org.seedstack.seed.cli.CliOption;

/**
 * Guice members injector that inject logger instances.
 *
 * @param <T> The type of class to inject.
 */
class CliMembersInjector<T> implements MembersInjector<T> {
    private final CliContext cliContext;
    private final String commandName;
    private final CliModel cliModel;

    CliMembersInjector(CliContext cliContext, String commandName, Set<Field> fields) {
        this.cliContext = cliContext;
        this.commandName = commandName;
        this.cliModel = new CliModel(fields);
    }

    @Override
    public void injectMembers(T t) {
        CommandLine commandLine = parseCommandLine(cliModel.getOptions(), cliContext.getArgs());
        injectArgs(commandLine, t);
        injectOptions(commandLine, t);
    }

    private void injectOptions(CommandLine commandLine, Object toInject) {
        List<CliOption> optionAnnotations = cliModel.getOptionAnnotations();
        List<Field> optionFields = cliModel.getOptionFields();

        for (int i = 0; i < optionAnnotations.size(); i++) {
            CliOption cliOption = optionAnnotations.get(i);
            Field field = optionFields.get(i);
            String[] value = null;

            if (cliOption.valueCount() > 0 || cliOption.valueCount() == -1) {
                if (commandLine.hasOption(cliOption.name())) {
                    value = commandLine.getOptionValues(cliOption.name());
                }

                if (value == null && cliOption.defaultValues().length > 0) {
                    value = cliOption.defaultValues();
                }

                if (value != null) {
                    if (cliOption.valueCount() != -1 && cliOption.valueCount() != value.length) {
                        throw SeedException.createNew(CliErrorCode.WRONG_NUMBER_OF_OPTION_ARGUMENTS)
                                .put("command", commandName)
                                .put("option", cliOption.name())
                                .put("given", value.length)
                                .put("required", cliOption.valueCount());
                    }

                    try {
                        Class<?> fieldType = field.getType();

                        makeAccessible(field);

                        if (String.class.isAssignableFrom(fieldType)) {
                            field.set(toInject, value[0]);
                        } else if (fieldType.isArray() && String.class.isAssignableFrom(fieldType.getComponentType())) {
                            field.set(toInject, value);
                        } else if (Map.class.isAssignableFrom(fieldType)) {
                            field.set(toInject, buildOptionArgumentMap(cliOption.name(), value));
                        } else {
                            throw SeedException.createNew(CliErrorCode.UNSUPPORTED_OPTION_FIELD_TYPE)
                                    .put("command", commandName)
                                    .put("option", cliOption.name())
                                    .put("fieldType", fieldType.getCanonicalName());
                        }
                    } catch (IllegalAccessException e) {
                        throw SeedException.wrap(e, CliErrorCode.UNABLE_TO_INJECT_OPTION)
                                .put("command", commandName)
                                .put("option", cliOption.name())
                                .put("field", field.getName());
                    }
                }
            } else {
                try {
                    makeAccessible(field).set(toInject, commandLine.hasOption(cliOption.name()));
                } catch (IllegalAccessException e) {
                    throw SeedException.wrap(e, CliErrorCode.UNABLE_TO_INJECT_OPTION)
                            .put("command", commandName)
                            .put("option", cliOption.name())
                            .put("field", field.getName());
                }
            }
        }
    }

    private void injectArgs(CommandLine commandLine, Object object) {
        Field argsField = cliModel.getArgsField();
        int mandatoryArgsCount = cliModel.getMandatoryArgsCount();

        if (argsField != null) {
            if (commandLine.getArgs().length < mandatoryArgsCount) {
                throw SeedException.createNew(CliErrorCode.MISSING_ARGUMENTS)
                        .put("command", commandName)
                        .put("required", mandatoryArgsCount)
                        .put("given", commandLine.getArgs().length);
            } else {
                try {
                    makeAccessible(argsField).set(object, commandLine.getArgs());
                } catch (IllegalAccessException e) {
                    throw SeedException.createNew(CliErrorCode.UNABLE_TO_INJECT_ARGUMENTS)
                            .put("command", commandName)
                            .put("field", argsField.getName());
                }
            }
        }
    }

    private CommandLine parseCommandLine(Options options, String[] args) {
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            throw SeedException.wrap(e, CliErrorCode.ERROR_PARSING_COMMAND_LINE)
                    .put("command", commandName)
                    .put("commandLine", Joiner.on(' ').join(args));
        }
        return commandLine;
    }

    private Map<String, String> buildOptionArgumentMap(String optionName, String[] optionArguments) {
        Map<String, String> optionArgumentsMap = new HashMap<>();

        if (optionArguments.length % 2 == 0) {
            for (int i = 0; i < optionArguments.length; i = i + 2) {
                optionArgumentsMap.put(optionArguments[i], optionArguments[i + 1]);
            }
        } else {
            throw SeedException.createNew(CliErrorCode.ODD_NUMBER_OF_OPTION_ARGUMENTS)
                    .put("command", commandName)
                    .put("option", optionName);
        }

        return optionArgumentsMap;
    }
}