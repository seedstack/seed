/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell.internal;

import com.google.common.base.Strings;
import jline.Terminal;
import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import jline.console.completer.CandidateListCompletionHandler;
import jline.console.completer.StringsCompleter;
import org.apache.shiro.concurrent.SubjectAwareExecutorService;
import org.apache.shiro.util.ThreadContext;
import org.apache.sshd.server.Environment;
import org.fusesource.jansi.Ansi;
import org.seedstack.seed.Application;
import org.seedstack.shed.exception.SeedException;
import org.seedstack.seed.spi.command.Command;
import org.seedstack.seed.spi.command.PrettyCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

class InteractiveShell extends AbstractShell {
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractiveShell.class);
    public static final String DETAILS_MESSAGE = "Details of the previous error below";
    public static final String WELCOME_MESSAGE = "    _______________    ______ ________   __ \n" +
            "   / __/ __/ __/ _ \\  / __/ // / __/ /  / / \n" +
            "  _\\ \\/ _// _// // / _\\ \\/ _  / _// /__/ /__\n" +
            " /___/___/___/____/ /___/_//_/___/____/____/\n";

    private ConsoleReader consoleReader;

    @Inject
    private Application application;

    private Terminal terminal;

    private PrintStream errorPrintStream;

    private SubjectAwareExecutorService ses;

    private boolean stackTraces;
    private boolean prettify = true;
    private OutputMode defaultOutputMode = OutputMode.JSON;

    @Override
    public void start(Environment environment) throws IOException {
        errorPrintStream = new PrintStream(errorStream, true);

        String user = environment.getEnv().get(Environment.ENV_USER);

        if (Strings.isNullOrEmpty(user)) {
            user = "unknown";
        }
        try {
            // Use our RemoteTerminal which does not depends on the platform.
            terminal = new RemoteTerminal(true);
            terminal.init();
        } catch (Exception e) {
            LOGGER.warn("Error during terminal detection, falling back to unsupported terminal");
            LOGGER.debug(DETAILS_MESSAGE, e);
            terminal = new UnsupportedTerminal();
        }

        consoleReader = new ConsoleReader(inputStream, outputStream, terminal);
        // Disable jline shutdownhook to avoid exception at application shutdown
        jline.internal.Configuration.getString("jline.shutdownhook", "false");

        consoleReader.addCompleter(new StringsCompleter(commandRegistry.getCommandList()));
        consoleReader.setCompletionHandler(new CandidateListCompletionHandler());
        consoleReader.setPrompt(user + "@" + application.getId() + "$ ");
        consoleReader.setHandleUserInterrupt(false);
        consoleReader.setHistoryEnabled(true);

        ses = new SubjectAwareExecutorService(Executors.newSingleThreadExecutor());
        ses.submit(this);
    }

    @Override
    public void destroy() {
        ses.shutdownNow();
        consoleReader.shutdown();

        ThreadContext.unbindSubject();
        ThreadContext.unbindSecurityManager();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        String line;
        try {
            clearScreen();
            printWelcomeMessage();

            line = consoleReader.readLine();
            while (line != null) {
                try {
                    if ("exit".equals(line)) {
                        break;
                    }

                    if (line.startsWith("?")) {
                        line = "help" + line.substring(1);
                    }

                    if ("clear".equals(line)) {
                        clearScreen();
                    } else if (line.startsWith("set")) {
                        String[] split = line.split(" ");
                        if (split.length < 2) {
                            throw SeedException.createNew(ShellErrorCode.MODE_SYNTAX_ERROR).put("value", line);
                        } else {
                            alterMode(split[1], (split.length == 3 ? split[2] : null));
                        }
                    } else {
                        Object result = null;
                        List<Command> commands = createCommandActions(line.trim());
                        Command lastCommand = null;

                        for (Command command : commands) {
                            result = command.execute(result);
                            lastCommand = command;
                        }

                        if (result != null) {
                            if (prettify && lastCommand instanceof PrettyCommand) {
                                consoleReader.println(processString(((PrettyCommand) lastCommand).prettify(result)));
                            } else {
                                if (result instanceof String) {
                                    consoleReader.println(processString((String) result));
                                } else {
                                    Command defaultOutputModeCommand = defaultOutputMode.getCommand();
                                    String output = (String) defaultOutputModeCommand.execute(result);
                                    if (defaultOutputModeCommand instanceof PrettyCommand) {
                                        output = ((PrettyCommand) defaultOutputModeCommand).prettify(output);
                                    }

                                    consoleReader.println(processString(output));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    errorPrintStream.print(switchToColor(Ansi.Color.RED));
                    if (stackTraces) {
                        e.printStackTrace(errorPrintStream);
                    } else {
                        if (e instanceof SeedException) {
                            String description = ((SeedException) e).getDescription();
                            String fix = ((SeedException) e).getFix();

                            if (description != null) {
                                errorPrintStream.println(description);
                            } else {
                                errorPrintStream.println(e.getMessage());
                            }

                            if (fix != null) {
                                errorPrintStream.println(fix);
                            }
                        } else {
                            errorPrintStream.println(e.getMessage());
                        }
                    }
                    errorPrintStream.print(resetColor());
                }

                line = consoleReader.readLine();
            }

            printGoodbyeMessage();
        } catch (IOException e) {
            LOGGER.warn("Interactive shell connection reset by peer");
            LOGGER.debug(DETAILS_MESSAGE, e);
        } catch (Throwable e) {
            LOGGER.error("Unexpected error during shell interactive session", e);
        } finally {
            exitCallback.onExit(0);
        }
    }

    private void alterMode(String modeName, String modeValue) {
        if ("output".equals(modeName)) {
            try {
                defaultOutputMode = OutputMode.valueOf(modeValue.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw SeedException.wrap(e, ShellErrorCode.ILLEGAL_MODE_OPTION).put("supportedOptions", Arrays.toString(OutputMode.values()));
            }
        } else if ("pretty".equals(modeName)) {
            if ("on".equalsIgnoreCase(modeValue)) {
                prettify = true;
            } else if ("off".equalsIgnoreCase(modeValue)) {
                prettify = false;
            } else {
                throw SeedException.createNew(ShellErrorCode.ILLEGAL_MODE_OPTION).put("supportedOptions", "on|off");
            }
        } else if ("stacktraces".equals(modeName)) {
            if ("on".equalsIgnoreCase(modeValue)) {
                stackTraces = true;
            } else if ("off".equalsIgnoreCase(modeValue)) {
                stackTraces = false;
            } else {
                throw SeedException.createNew(ShellErrorCode.ILLEGAL_MODE_OPTION).put("supportedOptions", "on|off");
            }
        } else {
            throw SeedException.createNew(ShellErrorCode.ILLEGAL_MODE).put("supportedModes", "output|pretty|stacktraces");
        }
    }

    private String processString(String value) {
        if (terminal.isAnsiSupported()) {
            return value;
        } else {
            return stripAnsiCharacters(value);
        }
    }

    private void clearScreen() throws IOException {
        if (terminal.isAnsiSupported()) {
            consoleReader.clearScreen();
        }
    }

    private String switchToColor(Ansi.Color color) {
        if (terminal.isAnsiSupported()) {
            return Ansi.ansi().fgBright(color).toString();
        } else {
            return "";
        }
    }

    private String resetColor() {
        if (terminal.isAnsiSupported()) {
            return Ansi.ansi().reset().toString();
        } else {
            return "";
        }
    }

    private String coloredString(String actual, Ansi.Color color) {
        if (terminal.isAnsiSupported()) {
            return Ansi.ansi().fgBright(color).a(actual).reset().toString();
        } else {
            return actual;
        }
    }

    private void printWelcomeMessage() throws IOException {
        consoleReader.println(coloredString(WELCOME_MESSAGE, Ansi.Color.GREEN));
        consoleReader.println("Call help (or ?) to see the list of command.");
        consoleReader.println();
    }

    private void printGoodbyeMessage() throws IOException {
        consoleReader.println("Bye!");
        consoleReader.println();
    }
}
