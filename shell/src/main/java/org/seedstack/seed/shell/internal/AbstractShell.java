/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell.internal;

import com.google.common.base.Strings;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.sshd.server.ExitCallback;
import org.seedstack.seed.CommandRegistry;
import org.seedstack.shed.exception.SeedException;
import org.seedstack.seed.spi.command.Command;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

abstract class AbstractShell implements org.apache.sshd.server.Command, Runnable {
    public static final String COMMAND_PATTERN = "([a-zA-Z][a-zA-Z0-9\\-]+:)?[a-zA-Z][a-zA-Z0-9\\-]+";
    private final CommandLineParser commandLineParser = new DefaultParser();

    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected OutputStream errorStream;
    protected ExitCallback exitCallback;

    @Inject
    protected CommandRegistry commandRegistry;

    @Override
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = new CRLFOutputStream(outputStream);
    }

    @Override
    public void setErrorStream(OutputStream outputStream) {
        this.errorStream = new CRLFOutputStream(outputStream);
    }

    @Override
    public void setExitCallback(ExitCallback exitCallback) {
        this.exitCallback = exitCallback;
    }

    protected List<Command> createCommandActions(String line) {
        if (Strings.isNullOrEmpty(line)) {
            return new ArrayList<>();
        }

        List<Command> commands = new ArrayList<>();

        Scanner scanner = new Scanner(new StringReader(line));

        String qualifiedName = null;
        List<String> args = new ArrayList<>();

        while (scanner.hasNext()) {
            if (qualifiedName == null) {
                if (scanner.hasNext(COMMAND_PATTERN)) {
                    qualifiedName = scanner.next(COMMAND_PATTERN);
                } else {
                    throw SeedException.createNew(ShellErrorCode.COMMAND_PARSING_ERROR);
                }
            } else {
                // Find next token respecting quoted strings
                String arg = scanner.findWithinHorizon("[^\"\\s]+|\"(\\\\.|[^\\\\\"])*\"", 0);
                if (arg != null) {
                    if ("|".equals(arg)) {
                        // unquoted pipe, we execute the command and store the result for the next one
                        commands.add(createCommandAction(qualifiedName, args));

                        qualifiedName = null;
                        args = new ArrayList<>();
                    } else {
                        // if it's a quoted string, unquote it
                        if (arg.startsWith("\"")) {
                            arg = arg.substring(1);
                        }
                        if (arg.endsWith("\"")) {
                            arg = arg.substring(0, arg.length() - 1);
                        }

                        // replace any escaped quote by real quote
                        args.add(arg.replaceAll("\\\\\"", "\""));
                    }
                } else {
                    throw SeedException.createNew(ShellErrorCode.COMMAND_SYNTAX_ERROR).put("value", scanner.next());
                }
            }
        }

        commands.add(createCommandAction(qualifiedName, args));

        return commands;
    }

    @SuppressWarnings("unchecked")
    protected Command createCommandAction(String qualifiedName, List<String> args) {
        if (Strings.isNullOrEmpty(qualifiedName)) {
            throw SeedException.createNew(ShellErrorCode.MISSING_COMMAND);
        }

        String commandScope;
        String commandName;

        if (qualifiedName.contains(":")) {
            String[] splitName = qualifiedName.split(":");
            commandScope = splitName[0].trim();
            commandName = splitName[1].trim();
        } else {
            commandScope = null;
            commandName = qualifiedName.trim();
        }

        // Build CLI options
        Options options = new Options();
        for (org.seedstack.seed.spi.command.Option option : commandRegistry.getOptionsInfo(commandScope, commandName)) {
            options.addOption(option.name(), option.longName(), option.hasArgument(), option.description());
        }

        // Parse the command options
        CommandLine cmd;
        try {
            cmd = commandLineParser.parse(options, args.toArray(new String[args.size()]));
        } catch (ParseException e) {
            throw SeedException.wrap(e, ShellErrorCode.OPTIONS_SYNTAX_ERROR);
        }

        Map<String, String> optionValues = new HashMap<>();
        for (Option option : cmd.getOptions()) {
            optionValues.put(option.getOpt(), option.getValue());
        }

        return commandRegistry.createCommand(commandScope, commandName, cmd.getArgList(), optionValues);
    }

    protected String stripAnsiCharacters(String value) {
        return value.replaceAll("\\e\\[[\\d;]*[^\\d;]", "");
    }
}
