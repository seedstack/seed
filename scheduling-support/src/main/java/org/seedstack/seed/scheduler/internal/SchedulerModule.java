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

import com.google.common.collect.Multimap;
import com.google.inject.PrivateModule;
import com.google.inject.multibindings.MapBinder;
import org.seedstack.seed.scheduler.api.ScheduledTasks;
import org.seedstack.seed.scheduler.api.Task;
import org.seedstack.seed.scheduler.api.TaskListener;
import org.quartz.Scheduler;

import java.util.Collection;
import java.util.Map;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 08/01/14
 */
class SchedulerModule extends PrivateModule {

    private final Collection<Class<?>> taskClasses;
    private final Multimap<Class<? extends Task>, Class<? extends TaskListener>> taskListenerMap;
    private final Scheduler scheduler;

    SchedulerModule(Collection<Class<?>> taskClasses, Scheduler scheduler, Multimap<Class<? extends Task>, Class<? extends TaskListener>> taskListenerMap) {
        this.taskClasses = taskClasses;
        this.scheduler = scheduler;
        this.taskListenerMap = taskListenerMap;
    }

    @Override
    protected void configure() {
        bind(GuiceTaskFactory.class);
        bind(ScheduledTasks.class).to(ScheduledTasksImpl.class);
        bind(Scheduler.class).toInstance(scheduler);
        bind(DelegateJobListener.class);

        for (Class<?> taskClass : taskClasses) {
            bind(taskClass);
        }

        MapBinder<String, TaskListener> mapBinder = MapBinder.newMapBinder(binder(), String.class, TaskListener.class);
        mapBinder.permitDuplicates();

        for (Map.Entry<Class<? extends Task>, Class<? extends TaskListener>> taskListenerEntry : taskListenerMap.entries()) {
            mapBinder.addBinding(taskListenerEntry.getKey().getCanonicalName()).to(taskListenerEntry.getValue());
        }

        requestStaticInjection(SchedulerPlugin.class);
        expose(ScheduledTasks.class);
    }
}
