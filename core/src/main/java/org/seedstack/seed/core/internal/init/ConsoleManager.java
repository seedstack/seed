/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;

import org.fusesource.jansi.AnsiOutputStream;
import org.fusesource.jansi.WindowsAnsiOutputStream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import static org.fusesource.jansi.internal.CLibrary.STDOUT_FILENO;
import static org.fusesource.jansi.internal.CLibrary.isatty;

public class ConsoleManager {
    private final PrintStream savedOut = System.out;
    private final PrintStream savedErr = System.err;

    private static class Holder {
        private static final ConsoleManager INSTANCE = new ConsoleManager();
    }

    public static synchronized void install() {
        System.setOut(new PrintStream(Holder.INSTANCE.wrapOutputStream(System.out)));
        System.setErr(new PrintStream(Holder.INSTANCE.wrapOutputStream(System.err)));
    }

    public static synchronized void uninstall() {
        synchronized (ConsoleManager.class) {
            System.setOut(Holder.INSTANCE.savedOut);
            System.setErr(Holder.INSTANCE.savedErr);
        }
    }

    private ConsoleManager() {
        // noop
    }

    private OutputStream wrapOutputStream(final OutputStream stream) {
        try {
            if (isIntelliJ() || isTTY() || isCygwin()) {
                return ansiOutput(stream);
            } else if (isWindows()) {
                return windowsOutput(stream);
            } else {
                return basicOutput(stream);
            }
        } catch (Throwable e) {
            return basicOutput(stream);
        }
    }

    private FilterOutputStream ansiOutput(OutputStream stream) {
        return new ColorOutputStream(stream);
    }

    private FilterOutputStream windowsOutput(OutputStream stream) {
        try {
            return new WindowsAnsiOutputStream(stream);
        } catch (Exception e) {
            return basicOutput(stream);
        }
    }

    private FilterOutputStream basicOutput(OutputStream stream) {
        return new AnsiOutputStream(stream);
    }

    private boolean isIntelliJ() {
        try {
            Class.forName("com.intellij.rt.execution.application.AppMain");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    private boolean isCygwin() {
        return isWindows() && System.getenv("TERM") != null;
    }

    private boolean isTTY() {
        try {
            return isatty(STDOUT_FILENO) != 0;
        } catch (Exception e) {
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
