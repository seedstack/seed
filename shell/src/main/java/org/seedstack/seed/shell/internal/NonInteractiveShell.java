/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell.internal;


import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;
import org.apache.shiro.concurrent.SubjectAwareExecutorService;
import org.apache.shiro.util.ThreadContext;
import org.apache.sshd.server.Environment;
import org.seedstack.seed.shell.internal.commands.JsonCommand;
import org.seedstack.seed.spi.command.Command;
import org.seedstack.seed.spi.command.StreamCommand;

import javax.inject.Inject;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

class NonInteractiveShell extends AbstractShell {
    private final String line;

    private PrintStream errorPrintStream;
    private PrintStream outputPrintStream;

    private SubjectAwareExecutorService ses;

    @Inject
    NonInteractiveShell(@Assisted String line) {
        this.line = line;
    }

    @Override
    public void start(Environment env) throws IOException {
        outputPrintStream = new PrintStream(outputStream, true);
        errorPrintStream = new PrintStream(errorStream, true);

        ses = new SubjectAwareExecutorService(Executors.newSingleThreadExecutor());
        ses.submit(this);
    }

    @Override
    public void destroy() {
        ses.shutdownNow();

        ThreadContext.unbindSubject();
        ThreadContext.unbindSecurityManager();
    }

    @Override
    public void run() {
        int returnCode = 0;

        try {
            List<String> args = new ArrayList<>();
            String[] splitLine = line.split("\\s");

            if (splitLine.length > 0) {
                if (splitLine.length > 1) {
                    args.addAll(Lists.newArrayList(Arrays.copyOfRange(splitLine, 1, splitLine.length)));
                }

                Command command = createCommandAction(splitLine[0], args);
                if (command instanceof StreamCommand) {
                    ((StreamCommand) command).execute(inputStream, outputStream, errorStream);
                } else {
                    Object result = command.execute(null);
                    if (result != null) {
                        if (result instanceof String) {
                            outputPrintStream.println(stripAnsiCharacters((String) result));
                        } else {
                            outputPrintStream.println(stripAnsiCharacters(new JsonCommand().execute(result)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            returnCode = 1;
            e.printStackTrace(errorPrintStream);
        } finally {
            exitCallback.onExit(returnCode);
        }
    }
}
