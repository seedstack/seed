/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell.internal.commands;

import org.seedstack.seed.security.RequiresRoles;
import org.seedstack.seed.spi.command.Command;
import org.seedstack.seed.spi.command.CommandDefinition;

@CommandDefinition(scope = "test", name = "denied", description = "Secured test command")
public class DeniedSecuredTestCommand implements Command {
    @Override
    @RequiresRoles("DENIED")
    public Object execute(Object object) throws Exception {
        return "denied";
    }
}
