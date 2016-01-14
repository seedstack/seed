/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli;

/**
 * @author adrien.lauer@mpsa.com
 */
@CliCommand("test2")
public class UnusedCommandLineHandler implements CommandLineHandler {
    static boolean called = false;

    @Override
    public Integer call() throws Exception {
        called = true;
        return 0;
    }
}
