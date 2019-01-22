/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.cli;

import org.seedstack.seed.cli.CliContext;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;

public class CliPlugin extends AbstractSeedPlugin {
    private CliContext cliContext;

    @Override
    public String name() {
        return "cli";
    }

    @Override
    protected void setup(SeedRuntime seedRuntime) {
        cliContext = seedRuntime.contextAs(CliContext.class);
    }

    @Override
    public Object nativeUnitModule() {
        if (cliContext != null) {
            return new CliModule(cliContext);
        } else {
            return null;
        }
    }
}
