/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
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
import org.fusesource.jansi.internal.CLibrary;
import org.seedstack.seed.ApplicationConfig;

public class ConsoleManager {
    private final PrintStream savedOut = System.out;
    private final PrintStream savedErr = System.err;

    private ConsoleManager() {
        // noop
    }

    public static ConsoleManager get() {
        return Holder.INSTANCE;
    }

    public synchronized void install(ApplicationConfig.ColorOutput colorOutput) {
        System.setOut(wrapPrintStream(System.out, CLibrary.STDOUT_FILENO, colorOutput));
        System.setErr(wrapPrintStream(System.err, CLibrary.STDERR_FILENO, colorOutput));
    }

    public synchronized void uninstall() {
        System.setOut(savedOut);
        System.setErr(savedErr);
    }

    private PrintStream wrapPrintStream(final PrintStream inputStream, int fileno,
            ApplicationConfig.ColorOutput colorOutput) {
        try {
            if (colorOutput == ApplicationConfig.ColorOutput.PASSTHROUGH || Boolean.getBoolean("jansi.passthrough")) {
                return inputStream;
            } else if (colorOutput == ApplicationConfig.ColorOutput.DISABLE || Boolean.getBoolean("jansi.strip")) {
                return strippedOutput(inputStream);
            } else if (colorOutput == ApplicationConfig.ColorOutput.ENABLE) {
                return ansiOutput(inputStream);
            } else if (colorOutput == ApplicationConfig.ColorOutput.AUTODETECT) {
                if (isXtermColor()) {
                    // enable color in recognized XTERM color modes
                    return ansiOutput(inputStream);
                } else if (isIntelliJ()) {
                    // enable color under Intellij
                    return ansiOutput(inputStream);
                } else {
                    // let Jansi handle other detection
                    return AnsiConsole.wrapPrintStream(inputStream, fileno);
                }
            } else {
                // Fallback to stripping ANSI codes
                return strippedOutput(inputStream);
            }
        } catch (Throwable e) {
            // If any error occurs, strip ANSI codes
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
}
