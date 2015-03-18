/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.scheduler.internal;

import org.seedstack.seed.scheduler.api.ExceptionPolicy;
import org.seedstack.seed.scheduler.api.Scheduled;
import org.seedstack.seed.scheduler.api.SchedulingContext;
import org.seedstack.seed.scheduler.api.Task;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * TaskDelegateJob is Quartz job which execute the call method of a Task.
 *
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 08/01/14
 */
class TaskDelegateJob implements Job {
    private final Task task;

    TaskDelegateJob(Task task) {
        this.task = task;
    }

    @SuppressWarnings({"unchecked", "ThrowableResultOfMethodCallIgnored"})
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        final SchedulingContext schedulingContext = new SchedulingContextImpl(context);

        try {
            task.execute(schedulingContext);
        } catch (final Exception ex) {
            final JobExecutionException e = new JobExecutionException("Error during job execution", ex);

            Scheduled scheduled = task.getClass().getAnnotation(Scheduled.class);
            if (scheduled != null) {
                if (ExceptionPolicy.REFIRE_IMMEDIATELY.equals(scheduled.exceptionPolicy())) {
                    e.setRefireImmediately(true);
                } else if (ExceptionPolicy.UNSCHEDULE_FIRING_TRIGGER.equals(scheduled.exceptionPolicy())) {
                    e.setUnscheduleFiringTrigger(true);
                } else if (ExceptionPolicy.UNSCHEDULE_ALL_TRIGGERS.equals(scheduled.exceptionPolicy())) {
                    e.setUnscheduleAllTriggers(true);
                }
            }

            throw e;
        }
    }
}
