/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed;

import org.seedstack.coffig.Config;

@Config("config")
public class ConfigConfig {
    private boolean watch = false;

    public boolean isWatch() {
        return watch;
    }

    public ConfigConfig setWatch(boolean watch) {
        this.watch = watch;
        return this;
    }
}
