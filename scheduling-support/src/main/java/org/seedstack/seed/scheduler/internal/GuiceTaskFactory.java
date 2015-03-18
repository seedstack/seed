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

import com.google.inject.Injector;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.scheduler.api.Task;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * This factory instantiates a {@link Job} wrapping a {@link org.seedstack.seed.scheduler.api.Task}.
 * The task will be initialised with its listeners.
 * A new Job will be created each time the associated trigger will fire.
 *
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 08/01/14
 */
class GuiceTaskFactory implements JobFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuiceTaskFactory.class);

    @Inject
    private Injector injector;

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        // create new Job
        Class<? extends Task> taskClass;
        TaskDelegateJob job;

        try {
            taskClass = (Class<? extends Task>) Class.forName(bundle.getJobDetail().getKey().getGroup());
        } catch (ClassNotFoundException e1) {
            LOGGER.error("Failed to retrieve Task's class {}", bundle.getJobDetail().getKey().getGroup(), e1);
            return null;
        } catch (ClassCastException e2) {
            LOGGER.error("Failed to cast Task's class {}", bundle.getJobDetail().getKey().getGroup(), e2);
            return null;
        }

        try {
            // Add the task to execute
            job = new TaskDelegateJob(injector.getInstance(taskClass));
        } catch (Exception e) {
            throw SeedException.wrap(e, SchedulerErrorCode.FAILED_TO_INSTANTIATE_TASK);
        }
        return job;
    }
}
