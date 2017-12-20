/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.internal;

import java.util.Optional;
import org.seedstack.seed.spi.SeedLauncher;
import org.seedstack.seed.testing.LaunchMode;
import org.seedstack.seed.testing.LaunchWith;
import org.seedstack.seed.testing.spi.TestContext;
import org.seedstack.seed.testing.spi.TestPlugin;
import org.seedstack.shed.reflect.Classes;

public class LaunchTestPlugin implements TestPlugin {
    @Override
    public boolean enabled(TestContext testContext) {
        return true;
    }

    @Override
    public LaunchMode launchMode(TestContext testContext) {
        LaunchWith launchWith = testContext.testClass().getAnnotation(LaunchWith.class);
        if (launchWith != null && launchWith.mode() != LaunchMode.ANY) {
            return launchWith.mode();
        } else {
            return LaunchMode.ANY;
        }
    }

    @Override
    public Optional<? extends SeedLauncher> launcher(TestContext testContext) {
        LaunchWith launchWith = testContext.testClass().getAnnotation(LaunchWith.class);
        if (launchWith != null && !SeedLauncher.class.equals(launchWith.value())) {
            return Optional.of(Classes.instantiateDefault(launchWith.value()));
        } else {
            return Optional.empty();
        }
    }
}
