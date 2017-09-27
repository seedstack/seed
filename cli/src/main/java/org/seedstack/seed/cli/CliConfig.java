/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.cli;

import org.seedstack.coffig.Config;

@Config("cli")
public class CliConfig {
    private String defaultCommand;

    public String getDefaultCommand() {
        return defaultCommand;
    }

    public boolean hasDefaultCommand() {
        return defaultCommand != null && !defaultCommand.isEmpty();
    }
}
