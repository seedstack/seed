/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiOutputStream;
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
        System.setOut(wrapPrintStream(System.out, colorOutput));
        System.setErr(wrapPrintStream(System.err, colorOutput));
    }

    public synchronized void uninstall() {
        System.setOut(savedOut);
        System.setErr(savedErr);
    }

    private PrintStream wrapPrintStream(final PrintStream printStream, ApplicationConfig.ColorOutput colorOutput) {
        OutputStream outputStream;
        try {
            if (colorOutput == ApplicationConfig.ColorOutput.PASSTHROUGH || Boolean.getBoolean("jansi.passthrough")) {
                outputStream = printStream;
            } else if (colorOutput == ApplicationConfig.ColorOutput.DISABLE || Boolean.getBoolean("jansi.strip")) {
                outputStream = basicOutput(printStream);
            } else if (colorOutput == ApplicationConfig.ColorOutput.ENABLE) {
                outputStream = ansiOutput(printStream);
            } else if (colorOutput == ApplicationConfig.ColorOutput.AUTODETECT) {
                if (isXtermColor()) {
                    // enable color in recognized XTERM color modes
                    outputStream = ansiOutput(printStream);
                } else if (isIntelliJ()) {
                    // enable color under Intellij
                    outputStream = ansiOutput(printStream);
                } else {
                    // let Jansi handle other detection
                    outputStream = AnsiConsole.wrapOutputStream(printStream);
                }
            } else {
                // Fallback to stripping ANSI codes
                outputStream = basicOutput(printStream);
            }
        } catch (Throwable e) {
            // If any error occurs, strip ANSI codes
            outputStream = basicOutput(printStream);
        }

        try {
            return new PrintStream(outputStream, false, Charset.defaultCharset().name());
        } catch (UnsupportedEncodingException e) {
            // if for some reason the default encoding is unsupported, fallback to passthrough
            return printStream;
        }
    }

    private FilterOutputStream basicOutput(PrintStream stream) {
        return new AnsiOutputStream(stream);
    }

    private FilterOutputStream ansiOutput(PrintStream stream) {
        return new ColorOutputStream(stream);
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

    private static class ColorOutputStream extends FilterOutputStream {
        private ColorOutputStream(OutputStream stream) {
            super(stream);
        }

        @Override
        public void close() throws IOException {
            write(AnsiOutputStream.RESET_CODE);
            flush();
            super.close();
        }
    }
}
