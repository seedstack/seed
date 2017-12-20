/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.command;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Add the ability to work on input, output and error streams to the the {@link Command} interface.
 */
public interface StreamCommand extends Command {

    /**
     * The action code to be executed in a stream context.
     *
     * @param inputStream  the input stream.
     * @param outputStream the output stream.
     * @param errorStream  the error stream.
     */
    void execute(InputStream inputStream, OutputStream outputStream, OutputStream errorStream);
}
