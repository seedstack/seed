/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.testing.junit4;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.google.inject.CreationException;
import javax.inject.Inject;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.testing.Expected;
import org.seedstack.seed.testing.LaunchMode;
import org.seedstack.seed.testing.LaunchWith;
import org.seedstack.seed.testing.junit4.fixtures.TestITLauncher;

@RunWith(SeedITRunner.class)
@LaunchWith(value = TestITLauncher.class, mode = LaunchMode.PER_TEST)
public class PerTestExpectedIT {
    @Inject
    private Object object;

    @BeforeClass
    public static void classSetup() {
        TestITLauncher.resetLaunchCount();
    }

    @Test
    @Expected(CreationException.class)
    public void injectionShouldNotWork() {
        assertThat(object).isNull();
    }

    @Test
    @Ignore
    public void testThatIgnoredTestAreNotInitialized() throws Exception {
        fail();
    }

    @AfterClass
    public static void afterClassTest() throws Exception {
        assertThat(TestITLauncher.getLaunchCount()).isEqualTo(1);

    }

}
