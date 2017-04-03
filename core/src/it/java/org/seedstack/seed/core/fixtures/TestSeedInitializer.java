/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.fixtures;

import org.seedstack.coffig.Coffig;
import org.seedstack.seed.spi.SeedInitializer;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSeedInitializer implements SeedInitializer {
    private static AtomicInteger callCount = new AtomicInteger(0);

    @Override
    public void onInitialization(Coffig configuration) {
        assertThat(callCount.getAndIncrement()).isEqualTo(0);
    }

    @Override
    public void onClose() {
        assertThat(callCount.getAndDecrement()).isEqualTo(1);
    }

    public static int getCallCount() {
        return callCount.get();
    }
}
