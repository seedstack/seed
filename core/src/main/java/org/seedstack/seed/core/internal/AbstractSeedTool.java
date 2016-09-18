/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import io.nuun.kernel.api.plugin.context.Context;
import org.seedstack.seed.spi.SeedTool;

public abstract class AbstractSeedTool extends AbstractSeedPlugin implements SeedTool {
    @Override
    public String name() {
        return toolName() + "-tool";
    }

    @Override
    public void start(Context context) {
        throw new IllegalStateException("A tool plugin cannot be started");
    }

    @Override
    public void stop() {
        throw new IllegalStateException("A tool plugin cannot be stopped");
    }

    public abstract String toolName();
}
