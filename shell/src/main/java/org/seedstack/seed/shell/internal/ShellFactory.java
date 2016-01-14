/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell.internal;

interface ShellFactory {
    /**
     * NonInteractiveShell factory method for Guice assisted injection.
     *
     * @param line the command line.
     * @return the instance.
     */
    NonInteractiveShell createNonInteractiveShell(String line);

    /**
     * InteractiveShell factory method for Guice assisted injection.
     *
     * @return the instance.
     */
    InteractiveShell createInteractiveShell();
}
