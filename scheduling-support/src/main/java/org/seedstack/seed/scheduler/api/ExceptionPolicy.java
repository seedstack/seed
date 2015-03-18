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

/**
 * This enum describes the behaviors of exception handling during the task execution.
 *
 *  <dl>
 *  <dt><b>REFIRE_IMMEDIATELY</b></dt>
 *  <dd>Immediately reexecutes the task. Be careful when using this option.
 *  If the task will always fail, it will be executed indefinitely.</dd>
 *
 *  <dt><b>UNSCHEDULE_FIRING_TRIGGER</b></dt>
 *  <dd>Unschedules the trigger which fire the task.
 *  It could be useful when the job fail due to a specific trigger.</dd>
 *
 *  <dt><b>UNSCHEDULE_ALL_TRIGGERS</b></dt>
 *  <dd>Unschedules all the triggers associated to a task.</dd>
 *
 *  <dt><b>NONE (Used by default)</b></dt>
 *  <dd>Do nothing</dd>
 *  </dl>
 */
public enum ExceptionPolicy {

    /**
     * Immediately re-execute the task. Be careful when using this option.
     * If the task will always fail, it will be executed indefinitely.
     */
    REFIRE_IMMEDIATELY,

    /**
     * Unschedule the trigger which fire the task.
     * It could be useful when the job fail due to a specific trigger.
     */
    UNSCHEDULE_FIRING_TRIGGER,

    /**
     * Unschedule all the triggers associated to a task.
     */
    UNSCHEDULE_ALL_TRIGGERS,

    /**
     * Do nothing on exception. Task will execute again on the next trigger fire.
     */
    NONE
}