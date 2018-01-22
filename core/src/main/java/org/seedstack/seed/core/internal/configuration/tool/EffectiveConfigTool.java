/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.configuration.tool;

import org.seedstack.seed.core.internal.AbstractSeedTool;

public class EffectiveConfigTool extends AbstractSeedTool {
    @Override
    public String toolName() {
        return "effective-config";
    }

    @Override
    public Integer call() throws Exception {
        System.out.println(getConfiguration().toString());
        return 0;
    }
}
