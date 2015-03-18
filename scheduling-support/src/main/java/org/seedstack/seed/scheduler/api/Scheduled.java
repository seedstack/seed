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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>{@code Scheduled} annotates {@link Task} to specified scheduling information.
 * <pre>
 * {@literal @}Scheduled("0/2 * * * * ?")
 * public class MyTask implements Task<Integer> {
 *     ...
 * }
 * </pre>
 *
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 08/01/14
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scheduled {

    public static final String DEFAULT = "DEFAULT";

    // Task

    /**
     * The {@code Task} name, must be unique within the group.
     *
     * @return {@code Task} name
     */
    String taskName() default DEFAULT;

    /**
     * Define whether or not the {@code Task} should be re-executed <br>
     * upon execution failure.
     */
    boolean requestRecovery() default false;

    /**
     * Define whether or not the {@code Task} should remain stored after it is
     * orphaned (no trigger points to it).
     */
    boolean storeDurably() default false;

    // Trigger

    /**
     * Defines the behavior when exception occurs during the task exception.
     *
     * @return {@code ExceptionPolicy}
     */
    ExceptionPolicy exceptionPolicy() default ExceptionPolicy.NONE;

    /**
     * The Trigger name, must be unique within the group.
     */
    String triggerName() default DEFAULT;

    /**
     * The cron expression to base the schedule on.
     */
    String value() default "";

    /**
     * The time zone for which the cron expression
     * of this trigger will be resolved.
     */
    String timeZoneId() default DEFAULT;

    /**
     * The trigger's priority.  When more than one trigger have the same
     * fire time, the scheduler will fire the one with the highest priority
     * first.
     */
    int priority() default 0;
}
