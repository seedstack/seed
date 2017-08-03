/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import org.seedstack.seed.cli.CliContext;

import static java.util.Objects.requireNonNull;

class ToolContext implements CliContext {
    private final String toolName;
    private final String[] args;

    ToolContext(String toolName, String[] args) {
        this.toolName = requireNonNull(toolName);
        this.args = requireNonNull(args).clone();
    }

    public String[] getArgs() {
        return args.clone();
    }

    public String getToolName() {
        return toolName;
    }
}
