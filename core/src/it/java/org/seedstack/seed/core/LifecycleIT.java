/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import io.nuun.kernel.api.Kernel;
import org.junit.Test;
import org.seedstack.seed.core.fixtures.TestLifecycleListener;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class LifecycleIT {
    @Test
    public void lifecycle_callbacks_are_invoked() {
        String token = UUID.randomUUID().toString();
        TestLifecycleListener.setToken(token);

        Kernel kernel = Seed.createKernel(null, null, false);

        assertThat(TestLifecycleListener.isStartHasBeenCalled(token)).isFalse();
        assertThat(TestLifecycleListener.isStopHasBeenCalled(token)).isFalse();

        kernel.start();

        assertThat(TestLifecycleListener.isStartHasBeenCalled(token)).isTrue();
        assertThat(TestLifecycleListener.isStopHasBeenCalled(token)).isFalse();

        Seed.disposeKernel(kernel);

        assertThat(TestLifecycleListener.isStartHasBeenCalled(token)).isTrue();
        assertThat(TestLifecycleListener.isStopHasBeenCalled(token)).isTrue();
    }
}
