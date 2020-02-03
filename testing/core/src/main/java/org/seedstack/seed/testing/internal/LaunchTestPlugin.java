/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
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
import org.seedstack.shed.reflect.Annotations;
import org.seedstack.shed.reflect.Classes;

public class LaunchTestPlugin implements TestPlugin {
    @Override
    public boolean enabled(TestContext testContext) {
        return true;
    }

    @Override
    public LaunchMode launchMode(TestContext testContext) {
        return Annotations.on(testContext.testClass())
                .includingMetaAnnotations()
                .find(LaunchWith.class)
                .map(LaunchWith::mode)
                .orElse(LaunchMode.ANY);
    }

    @Override
    public Optional<? extends SeedLauncher> launcher(TestContext testContext) {
        return Annotations.on(testContext.testClass())
                .includingMetaAnnotations()
                .find(LaunchWith.class)
                .filter(launchWith -> !SeedLauncher.class.equals(launchWith.value()))
                .map(LaunchWith::value)
                .map(Classes::instantiateDefault);
    }

    @Override
    public boolean separateThread(TestContext testContext) {
        return Annotations.on(testContext.testClass())
                .includingMetaAnnotations()
                .find(LaunchWith.class)
                .map(LaunchWith::separateThread)
                .orElse(false);
    }
}
