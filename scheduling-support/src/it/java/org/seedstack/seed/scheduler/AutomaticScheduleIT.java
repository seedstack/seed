/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.scheduler;

import org.seedstack.seed.it.SeedITRunner;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 09/01/14
 */
@RunWith(SeedITRunner.class)
public class AutomaticScheduleIT {
    static CountDownLatch countDownLatch = new CountDownLatch(2);
    static int invocationCount1 = 0;
    static int invocationCount2 = 0;
    static boolean beforeCalled = false;
    static boolean afterCalled = false;
    static boolean onExceptionCalled = false;

    @Test
    public void automatically_timed_task() throws Exception {
        if (!countDownLatch.await(10, TimeUnit.SECONDS))
            fail("timeout during automatically timed task wait");

        Assertions.assertThat(beforeCalled).isTrue();
        Assertions.assertThat(afterCalled).isFalse();
        Assertions.assertThat(onExceptionCalled).isTrue();

        // if the test is slow it will execute TimedTask1 multiple times
        Assertions.assertThat(invocationCount1).isGreaterThanOrEqualTo(1);
        Assertions.assertThat(invocationCount2).isGreaterThanOrEqualTo(1);
    }
}
