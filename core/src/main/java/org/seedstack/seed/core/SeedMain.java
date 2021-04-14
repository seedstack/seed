/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

/**
 * Built-in main class delegating to {@link Seed#launch(String[])}.
 */
public class SeedMain {
    /**
     * Entry point of SeedStack non-managed applications (launched from the command-line).
     *
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        Seed.launch(args);
    }
}
