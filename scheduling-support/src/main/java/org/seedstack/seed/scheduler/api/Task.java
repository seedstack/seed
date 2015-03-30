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
 * {@code Task} classes could be scheduled:
 * By adding a {@link Scheduled} annotation
 *
 * <pre>
 * {@literal @}Scheduled("0/2 * * * * ?")
 * </pre>
 *
 * Or programmatically with a {@link ScheduledTaskBuilder}
 *
 * <pre>
 * {@literal @}Inject
 * private ScheduledTaskBuilderFactory factory;
 * ...
 * factory.createSchedulerBuilder(MyTask.class).withCronExpression("0/2 * * * * ?").schedule();
 * </pre>
 *
 * @author pierre.thirouin@ext.mpsa.com
 * @author david.scherrer@ext.mpsa.com
 */
public interface Task {
    /**
     * This method is called by the scheduler when the task is executed.
     *
     * @param sc the associated scheduling context
     * @throws Exception if something goes wrong during task execution.
     */
    void execute(SchedulingContext sc) throws Exception;
}
