/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell.commands;

import org.seedstack.seed.core.spi.command.Command;
import org.seedstack.seed.core.spi.command.CommandDefinition;
import org.seedstack.seed.security.api.annotations.RequiresRoles;

@CommandDefinition(scope = "test", name = "denied", description = "Secured test command")
public class DeniedSecuredTestCommand implements Command {
    @Override
    @RequiresRoles("DENIED")
    public Object execute(Object object) throws Exception {
        return "denied";
    }
}
