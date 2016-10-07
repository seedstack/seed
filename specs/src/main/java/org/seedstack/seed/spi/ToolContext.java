/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.spi;

import org.seedstack.seed.cli.CliContext;

/**
 * Describes a tool execution context.
 */
public class ToolContext implements CliContext {
    private final String toolName;
    private final String[] args;

    public ToolContext(String toolName, String[] args) {
        this.toolName = toolName;
        this.args = args;
    }

    public String[] getArgs() {
        return args;
    }

    public String getToolName() {
        return toolName;
    }
}
