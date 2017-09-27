/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.fixtures;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.spi.SeedInitializer;

public class TestSeedInitializer implements SeedInitializer {
    private static AtomicInteger beforeCallCount = new AtomicInteger(0);
    private static AtomicInteger onCallCount = new AtomicInteger(0);
    private static AtomicInteger afterCallCount = new AtomicInteger(0);

    public static int getBeforeCallCount() {
        return beforeCallCount.get();
    }

    public static int getOnCallCount() {
        return onCallCount.get();
    }

    public static int getAfterCallCount() {
        return afterCallCount.get();
    }

    @Override
    public void beforeInitialization() {
        assertThat(beforeCallCount.getAndIncrement()).isEqualTo(0);
    }

    @Override
    public void onInitialization(Coffig configuration) {
        assertThat(onCallCount.getAndIncrement()).isEqualTo(0);
    }

    @Override
    public void afterInitialization() {
        assertThat(afterCallCount.getAndIncrement()).isEqualTo(0);
    }

    @Override
    public void onClose() {
        throw new IllegalStateException("Should not be called from tests");
    }
}
