/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it;

import io.nuun.kernel.api.Kernel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.nuun.kernel.core.NuunCore.createKernel;
import static io.nuun.kernel.core.NuunCore.newKernelConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

public class WithoutRunnerIT {
    static Kernel underTest;

    @BeforeClass
    public static void beforeClass() throws Exception {
        underTest = createKernel(newKernelConfiguration());
        underTest.init();
        underTest.start();
    }

    @AfterClass
    public static void afterClass() {
        underTest.stop();
    }

    @Test
    public void it_plugin_is_working_even_without_seed_runner() {
        assertThat(underTest).isNotNull();
    }
}
