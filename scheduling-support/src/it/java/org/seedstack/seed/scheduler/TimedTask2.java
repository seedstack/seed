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

import org.seedstack.seed.core.api.Logging;
import org.seedstack.seed.scheduler.api.SchedulingContext;
import org.seedstack.seed.scheduler.api.Task;
import org.slf4j.Logger;


public class TimedTask2 implements Task {
    @Logging
    Logger logger;

    @Override
    public void execute(SchedulingContext sc) throws Exception {
        logger.info("Executing timed task 2");
        AutomaticScheduleIT.invocationCount2++;
        AutomaticScheduleIT.countDownLatch.countDown();
    }
}
