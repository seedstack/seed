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

import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.scheduler.api.TaskListener;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class DelegateJobListener implements JobListener {
    @Inject
    Map<String, Set<TaskListener>> taskListeners = new HashMap<String, Set<TaskListener>>();

    @Override
    public String getName() {
        return "delegateJobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        Set<TaskListener> jobTaskListeners = taskListeners.get(context.getJobDetail().getKey().getGroup());
        if (jobTaskListeners != null) {
            for (TaskListener jobTaskListener : jobTaskListeners) {
                try {
                    jobTaskListener.before(new SchedulingContextImpl(context));
                } catch(Exception e) {
                    throw SeedException.wrap(e, SchedulerErrorCode.EXCEPTION_IN_LISTENER).put("method", "before").put("listenerClass", jobTaskListener.getClass());
                }
            }
        }
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        // job veto is not supported by TaskListener
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        Set<TaskListener> jobTaskListeners = taskListeners.get(context.getJobDetail().getKey().getGroup());
        if (jobTaskListeners != null) {
            for (TaskListener jobTaskListener : jobTaskListeners) {
                if (jobException != null) {
                    try {
                        jobTaskListener.onException(new SchedulingContextImpl(context), jobException);
                    } catch(Exception e) {
                        throw SeedException.wrap(e, SchedulerErrorCode.EXCEPTION_IN_LISTENER).put("method", "onException").put("listenerClass", jobTaskListener.getClass());
                    }
                } else {
                    try {
                        jobTaskListener.after(new SchedulingContextImpl(context));
                    } catch(Exception e) {
                        throw SeedException.wrap(e, SchedulerErrorCode.EXCEPTION_IN_LISTENER).put("method", "after").put("listenerClass", jobTaskListener.getClass());
                    }
                }
            }
        }
    }
}
