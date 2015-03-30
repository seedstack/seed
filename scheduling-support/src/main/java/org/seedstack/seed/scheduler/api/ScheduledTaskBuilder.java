/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.scheduler.api;

import java.util.TimeZone;

/**
 * DSL to produce and schedule a {@link Task}. To use the {@code ScheduledTaskBuilder},
 * inject a {@link ScheduledTasks} and create a new {@code ScheduledTaskBuilder}.
 * <pre>
 * ScheduledTaskBuilder schedulerBuilder = factory.scheduledTask(MyTask.class);
 * schedulerBuilder.withCronExpression("0/2 * * * * ?").schedule();
 * </pre>
 *
 * @see ScheduledTasks
 */
public interface ScheduledTaskBuilder {

    /**
     * Instructs the {@code Scheduler} whether or not the {@code Task} should
     * be re-executed if a {@code recovery} or {@code fail-over} situation is
     * encountered.
     *
     * @param requestRecovery The activation flag
     * @return This builder instance
     */
    ScheduledTaskBuilder withRequestRecovery(boolean requestRecovery);

    /**
     * Whether or not the {@code Task} should remain stored after it is
     * orphaned (no {@code Trigger}s point to it).
     *
     * @param storeDurably The activation flag
     * @return This builder instance
     */
    ScheduledTaskBuilder withStoreDurably(boolean storeDurably);

    /**
     * Sets the {@code Trigger} name, must be unique within the group.
     *
     * @param triggerName The {@code Trigger} name, must be unique within the group
     * @return This builder instance
     */
    ScheduledTaskBuilder withTriggerName(String triggerName);

    /**
     * Sets the cron expression to base the schedule on.
     *
     * @param cronExpression The cron expression to base the schedule on
     * @return This builder instance
     */
    ScheduledTaskBuilder withCronExpression(String cronExpression);

    /**
     * Sets the time zone for which the {@code cronExpression} of this
     * {@code CronTrigger} will be resolved.
     *
     * @param timeZone The time zone for which the {@code cronExpression}
     *                 of this {@code CronTrigger} will be resolved.
     * @return This builder instance
     */
    ScheduledTaskBuilder withTimeZone(TimeZone timeZone);



    /**
     * Sets the {@code Task}'s identity
     * @param taskName The {@code Task}'s name
     * @return This builder instance
     */
    ScheduledTaskBuilder withTaskName(String taskName);
    
    /**
     * Sets the Trigger's priority.<br>  
     * 
     * When more than one Trigger have the same fire time, <br>
     * the scheduler will fire the one with the highest priority first.<br>
     *
     * @param priority -  The Trigger's priority
     * @return This builder instance
     */
    ScheduledTaskBuilder withPriority(int priority);

    /**
     * Sets the {@code Trigger} that will be used to schedule
     * the {@code Task}.
     * <p>
     * Be aware that using using this method will override any other
     * {@code Trigger}-related operation, like {@link #withTriggerName(String)}
     * or {@link #withTimeZone(java.util.TimeZone)}
     *
     * @param trigger The {@code Trigger} to associate with the {@code Task}
     * @return This builder instance
     */
    ScheduledTaskBuilder withTrigger(Object trigger);

    /**
     * Requests an existing trigger (sharing the same key as the new trigger) for this task to
     * be replaced with the new trigger.
     *
     * @return This builder instance
     */
    ScheduledTaskBuilder updateExistingTrigger();

    /**
     * Bind a {@code Task} to a trigger and schedule.
     */
    void schedule();

    /**
     * Reschedule the {@code Task} with a new trigger
     * <p>
     * The new trigger should be provided either using DSL<br>
     * withTrigger() or throw @Scheduled CRON definition.<br>
     * The DSL can precise attributes of the new trigger such as withTriggerName
     * <p>
     * Arguments provide the previous trigger identification to be replaced
     *
     * @param triggerName the name of the trigger to reschedule.
     */
	void reschedule(String triggerName);


    /**
     * Unschedule the {@code Task} with provided trigger identification.<br>
     * triggerGroup is the Class of the {@code Task} implementation
     *
     * @param triggerName the name of the trigger to unschedule.
     */
	void unschedule(String triggerName);

}
