/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;

import java.io.PrintStream;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiPrintStream;
import org.fusesource.jansi.FilterPrintStream;
import org.fusesource.jansi.WindowsAnsiPrintStream;
import org.fusesource.jansi.internal.CLibrary;
import org.seedstack.seed.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleManager.class);
    private final PrintStream savedOut = System.out;
    private final PrintStream savedErr = System.err;

    private ConsoleManager() {
        // noop
    }

    public static ConsoleManager get() {
        return Holder.INSTANCE;
    }

    public synchronized void install(ApplicationConfig.ColorOutput colorOutput) {
        System.setOut(wrapPrintStream(System.out, FileNo.STDOUT, colorOutput, true));
        System.setErr(wrapPrintStream(System.err, FileNo.STDERR, colorOutput, false));
    }

    public synchronized void uninstall() {
        System.setOut(savedOut);
        System.setErr(savedErr);
    }

    private PrintStream wrapPrintStream(final PrintStream inputStream, FileNo fileno,
            ApplicationConfig.ColorOutput colorOutput, boolean log) {
        try {
            if (colorOutput == ApplicationConfig.ColorOutput.PASSTHROUGH) {
                if (log) {
                    LOGGER.info("Color output passthrough: leaving stdout and stderr untouched");
                }
                return inputStream;
            } else if (colorOutput == ApplicationConfig.ColorOutput.DISABLE) {
                if (log) {
                    LOGGER.info("Color output disabled: stripping stdout and stderr of ANSI codes");
                }
                return strippedOutput(inputStream);
            } else if (colorOutput == ApplicationConfig.ColorOutput.ENABLE) {
                if (log) {
                    LOGGER.info("Color output enabled: allowing ANSI codes on stdout and stderr");
                }
                return ansiOutput(inputStream);
            } else if (colorOutput == ApplicationConfig.ColorOutput.AUTODETECT) {
                if (isXtermColor()) {
                    // enable color in recognized XTERM color modes
                    if (log) {
                        LOGGER.info("XTERM detected: allowing ANSI codes on stdout and stderr");
                    }
                    return ansiOutput(inputStream);
                } else if (isIntelliJ()) {
                    // enable color under Intellij
                    if (log) {
                        LOGGER.info("IntelliJ detected: allowing ANSI codes on stdout and stderr");
                    }
                    return ansiOutput(inputStream);
                } else {
                    // let Jansi handle other detection
                    PrintStream result;
                    switch (fileno) {
                        case STDOUT:
                            result = AnsiConsole.wrapPrintStream(inputStream, CLibrary.STDOUT_FILENO);
                            break;
                        case STDERR:
                            result = AnsiConsole.wrapPrintStream(inputStream, CLibrary.STDERR_FILENO);
                            break;
                        default:
                            result = strippedOutput(inputStream);
                            break;
                    }
                    if (log) {
                        if (result instanceof WindowsAnsiPrintStream) {
                            LOGGER.info("Windows console detected: using native coloring for stdout and stderr");
                        } else if (result instanceof AnsiPrintStream) {
                            LOGGER.info("ANSI not supported: stripping stdout and stderr of ANSI codes");
                        } else {
                            LOGGER.info("ANSI supported: allowing ANSI codes on stdout and stderr");
                        }
                    }
                    return result;
                }
            } else {
                // Fallback to stripping ANSI codes
                if (log) {
                    LOGGER.info("Fallback to stripping ANSI codes");
                }
                return strippedOutput(inputStream);
            }
        } catch (Throwable e1) {
            // If any error occurs, strip ANSI codes
            if (log) {
                LOGGER.info("Error loading Jansi (unsupported platform ?): stripping stdout and stderr of ANSI codes");
            }
            return strippedOutput(inputStream);
        }
    }

    private AnsiPrintStream strippedOutput(PrintStream stream) {
        return new AnsiPrintStream(stream);
    }

    private ColorPrintStream ansiOutput(PrintStream stream) {
        return new ColorPrintStream(stream);
    }

    private boolean isXtermColor() {
        String term = System.getenv("TERM");
        return "xterm-256color".equals(term) ||
                "xterm-color".equals(term) ||
                "xterm".equals(term);
    }

    private boolean isIntelliJ() {
        if (System.getProperty("java.class.path").contains("idea_rt.jar")) {
            return true;
        } else {
            try {
                Class.forName("com.intellij.rt.execution.application.AppMain");
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }

    private static class Holder {
        private static final ConsoleManager INSTANCE = new ConsoleManager();
    }

    private static class ColorPrintStream extends FilterPrintStream {
        private ColorPrintStream(PrintStream stream) {
            super(stream);
        }

        @Override
        public void close() {
            ps.print(AnsiPrintStream.RESET_CODE);
            ps.flush();
            super.close();
        }
    }

    private enum FileNo {
        STDOUT,
        STDERR
    }
}
