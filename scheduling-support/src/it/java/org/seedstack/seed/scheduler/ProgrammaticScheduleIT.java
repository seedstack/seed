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
import org.seedstack.seed.scheduler.api.ScheduledTasks;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.Trigger;

import javax.inject.Inject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 09/01/14
 */
@RunWith(SeedITRunner.class)
public class ProgrammaticScheduleIT {
    static CountDownLatch countDownLatch = new CountDownLatch(1);
    static int invocationCount3 = 0;

    @Inject
    private ScheduledTasks scheduledTasks;

    @Test
    public void programmatically_timed_task() throws Exception {
        Trigger trigger = newTrigger()
                .withIdentity(triggerKey("Trigger3", "TriggerGroup"))
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(1))
                .build();

        scheduledTasks.scheduledTask(TimedTask3.class).withTaskName("Task3").withTrigger(trigger).withPriority(10).schedule();

        if (!countDownLatch.await(10, TimeUnit.SECONDS))
            fail("timeout during programatically timed task wait");

        Assertions.assertThat(invocationCount3).isEqualTo(1);
    }
}
