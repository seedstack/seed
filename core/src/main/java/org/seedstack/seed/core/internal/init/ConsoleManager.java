/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;

import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiOutputStream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ConsoleManager {
    private PrintStream savedOut;
    private PrintStream savedErr;

    public synchronized void install() {
        OutputStream out = wrapOutputStream(System.out);
        OutputStream err = wrapOutputStream(System.err);
        savedOut = System.out;
        System.setOut(new PrintStream(out));
        savedErr = System.err;
        System.setErr(new PrintStream(err));
    }

    public synchronized void uninstall() {
        System.setOut(savedOut);
        System.setErr(savedErr);
    }

    public boolean isColorSupported() {
        // we cannot know if color is supported
        return false;
    }

    private OutputStream wrapOutputStream(final OutputStream stream) {
        try {
            if (Boolean.getBoolean("jansi.passthrough")) {
                // honor jansi passthrough
                return stream;
            } else if (Boolean.getBoolean("jansi.strip")) {
                // honor jansi strip
                return basicOutput(stream);
            } else if (isXtermColor()) {
                // enable color in recognized XTERM color modes
                return ansiOutput(stream);
            } else if (isIntelliJ()) {
                // enable color under Intellij
                return ansiOutput(stream);
            } else {
                // let Jansi handle other detection
                return AnsiConsole.wrapOutputStream(stream);
            }
        } catch (Throwable e) {
            // If any error occurs, strip ANSI codes
            return basicOutput(stream);
        }
    }

    private FilterOutputStream basicOutput(OutputStream stream) {
        return new AnsiOutputStream(stream);
    }

    private FilterOutputStream ansiOutput(OutputStream stream) {
        return new ColorOutputStream(stream);
    }

    private boolean isXtermColor() {
        String term = System.getenv("TERM");
        return "xterm-256color".equals(term) ||
                "xterm-color".equals(term) ||
                "xterm".equals(term);
    }

    private boolean isIntelliJ() {
        try {
            Class.forName("com.intellij.rt.execution.application.AppMain");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static class ColorOutputStream extends FilterOutputStream {
        private ColorOutputStream(OutputStream stream) {
            super(stream);
        }

        @Override
        public void close() throws IOException {
            write(AnsiOutputStream.REST_CODE);
            flush();
            super.close();
        }
    }
}
