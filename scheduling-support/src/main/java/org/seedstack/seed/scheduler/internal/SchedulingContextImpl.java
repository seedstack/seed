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

import org.seedstack.seed.scheduler.api.SchedulingContext;
import org.quartz.JobExecutionContext;

import java.util.Date;


/**
 * @author david.scherrer@ext.mpsa.com
 *         Date: 07/03/14
 */

class SchedulingContextImpl implements SchedulingContext {

    // Task

    /**
     * The Task name, must be unique within the group.
     */
    private final String taskName;


    /**
     * Instructs the Scheduler whether or not the Task should
     * be re-executed if a recovery or fail-over situation is
     * encountered.
     */
    private final boolean requestRecovery;

    /**
     * Whether or not the Task should remain stored after it is
     * orphaned (no Triggers point to it).
     */
    private final boolean storeDurably;

    // Trigger

    /**
     * The Trigger name, must be unique within the group.
     */
    private final String triggerName;

    /**
     * The Trigger's priority.  When more than one Trigger have the same
     * fire time, the scheduler will fire the one with the highest priority
     * first.
     */
    private final int triggerPriority;

    /**
     * actual fire time of the current Task run
     */
    private final Date currentFireDate;
    /**
     * fire time of the previous Task run
     */
    private final Date previousFireDate;
    /**
     * fire time of the next Task run
     */
    private final Date nextFireDate;

    /**
     * The amount of time the job ran for (in milliseconds).
     * The returned value will be -1 until the Task has actually completed (or thrown an exception),
     * and is therefore generally only useful to TaskListeners.
     */
    private final long taskRuntime;

    /**
     * actual scheduled time of the current Task run
     */
    private final Date scheduledFireDate;

    /**
     * Number of times the Trigger has been Task refired
     */
    private final int triggerRefireCount;

    /**
     * Trigger planned date of final run
     */
    private final Date triggerFinalFireDate;

    /**
     * Trigger planned end time
     */
    private final Date triggerEndDate;

    /**
     * Trigger planned start time
     */
    private final Date triggerStartDate;


    SchedulingContextImpl(JobExecutionContext context) {

        taskName = context.getJobDetail().getKey().getName();
        storeDurably = context.getJobDetail().isDurable();
        requestRecovery = context.getJobDetail().requestsRecovery();

        scheduledFireDate = context.getScheduledFireTime();
        currentFireDate = context.getFireTime();
        previousFireDate = context.getPreviousFireTime();
        nextFireDate = context.getNextFireTime();

        taskRuntime = context.getJobRunTime();

        triggerRefireCount = context.getRefireCount();
        triggerEndDate = context.getTrigger().getEndTime();
        triggerFinalFireDate = context.getTrigger().getFinalFireTime();
        triggerName = context.getTrigger().getKey().getName();
        triggerStartDate = context.getTrigger().getStartTime();
        triggerPriority = context.getTrigger().getPriority();

    }

    public String getTaskName() {
        return taskName;
    }

    public boolean isRequestRecovery() {
        return requestRecovery;
    }

    public boolean isStoreDurably() {
        return storeDurably;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public int getTriggerPriority() {
        return triggerPriority;
    }

    public Date getCurrentFireDate() {
        return currentFireDate;
    }

    public Date getPreviousFireDate() {
        return previousFireDate;
    }

    public Date getNextFireDate() {
        return nextFireDate;
    }

    public long getTaskRuntime() {
        return taskRuntime;
    }

    public Date getScheduledFireDate() {
        return scheduledFireDate;
    }

    public int getTriggerRefireCount() {
        return triggerRefireCount;
    }

    public Date getTriggerFinalFireDate() {
        return triggerFinalFireDate;
    }

    public Date getTriggerEndDate() {
        return triggerEndDate;
    }

    public Date getTriggerStartDate() {
        return triggerStartDate;
    }
}
