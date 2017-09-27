/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.cli;

/**
 * This interface is implemented by runtime context classes holding command-line arguments. This allows SeedStack
 * plugins to
 * retrieve those arguments with:
 * <pre>{@code
 * public void setup(SeedRuntime seedRuntime) {
 *         cliContext = seedRuntime.contextAs(CliContext.class);
 * }
 * }
 * </pre>
 */
public interface CliContext {

    /**
     * @return the command-line arguments as passed to the program.
     */
    String[] getArgs();

}
