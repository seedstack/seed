/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.it;

import static org.assertj.core.api.Assertions.assertThat;

import io.nuun.kernel.api.Kernel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seedstack.seed.core.Seed;

public class WithoutRunnerIT {
    private static Kernel kernel;

    @BeforeClass
    public static void beforeClass() throws Exception {
        kernel = Seed.createKernel();
    }

    @AfterClass
    public static void afterClass() {
        Seed.disposeKernel(kernel);
    }

    @Test
    public void it_plugin_is_working_even_without_seed_runner() {
        assertThat(kernel).isNotNull();
    }
}
