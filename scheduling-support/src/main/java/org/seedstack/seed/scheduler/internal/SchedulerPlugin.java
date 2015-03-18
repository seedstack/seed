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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.scheduler.api.Scheduled;
import org.seedstack.seed.scheduler.api.ScheduledTasks;
import org.seedstack.seed.scheduler.api.Task;
import org.seedstack.seed.scheduler.api.TaskListener;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.lang.StringUtils;
import org.kametic.specifications.Specification;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import javax.inject.Inject;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 08/01/14
 */
public class SchedulerPlugin extends AbstractPlugin {

    private Specification<Class<?>> specificationForJobs;
    private Specification<Class<?>> specificationForJobListeners;
    private Collection<Class<?>> jobClasses;
    private Multimap<Class<? extends Task>, Class<? extends TaskListener>> jobListenerMap = ArrayListMultimap.create();
    private Scheduler scheduler;

    @Inject
    static DelegateJobListener delegateJobListener;
    @Inject
    static GuiceTaskFactory guiceTaskFactory;
    @Inject
    static ScheduledTasks scheduledTasks;

    @Override
    public String name() {
        return "seed-scheduler-plugin";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        specificationForJobs = classImplements(Task.class);
        specificationForJobListeners = classImplements(TaskListener.class);
        return classpathScanRequestBuilder().specification(specificationForJobs).specification(specificationForJobListeners).build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState init(InitContext initContext) {
        Map<Specification, Collection<Class<?>>> scannedTypesBySpecification = initContext.scannedTypesBySpecification();

        // Associates - scan for nativeUnitModule
        jobClasses = scannedTypesBySpecification.get(specificationForJobs);

        Collection<Class<?>> listenerClasses = scannedTypesBySpecification.get(specificationForJobListeners);
        for (Class<?> listenerClass : listenerClasses) {
            if (TaskListener.class.isAssignableFrom(listenerClass)) {
                // Get the type of Job to listen
                Type typeVariable = getParametrizedTypeOfJobListener(listenerClass);
                if (typeVariable != null && Task.class.isAssignableFrom((Class<?>) typeVariable)) {
                    // bind the Task to the listener
                    jobListenerMap.put((Class<? extends Task>) typeVariable, (Class<? extends TaskListener>) listenerClass);
                }
            }
        }

        // Initialises the scheduler and adds jobs
        StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
        try {
            this.scheduler = schedulerFactory.getScheduler();
        } catch (Exception e) {
            throw SeedException.wrap(e, SchedulerErrorCode.SCHEDULER_ERROR);
        }

        return InitState.INITIALIZED;
    }


    /**
     * Return the type parameter of the TaskListener interface.
     *
     * @param listenerClass class to check
     * @return type which extends Task
     * @throws SeedException if no parameter type is present
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private Type getParametrizedTypeOfJobListener(Class<?> listenerClass) {
        Type[] interfaces = listenerClass.getGenericInterfaces();
        Type[] typeParameters = null;
        for (Type anInterface : interfaces) {
            // Checks if the class get parameters
            if (!(anInterface instanceof ParameterizedType)) {
                continue;
            }
            // Gets rawType to check if the interface is TaskListener
            Class interfaceClass = (Class) ((ParameterizedType) anInterface).getRawType();
            if (TaskListener.class.isAssignableFrom(interfaceClass)) {
                typeParameters = ((ParameterizedType) anInterface).getActualTypeArguments();
                break;
            }
        }
        if (typeParameters == null || typeParameters.length == 0) {
            throw SeedException.createNew(SchedulerErrorCode.MISSING_TYPE_PARAMETER)
                    .put("class", listenerClass);
        }

        return typeParameters[0];
    }

    @Override
    public void start(Context context) {
        super.start(context);

        try {
            // Configure scheduler
            scheduler.setJobFactory(guiceTaskFactory);
            scheduler.getListenerManager().addJobListener(delegateJobListener);

            // Schedule declarative jobs (@Scheduled)
            scheduleJobs();

            // Start scheduler
            scheduler.start();
        } catch (Exception e) {
            throw SeedException.wrap(e, SchedulerErrorCode.SCHEDULER_FAILED_TO_START);
        }
    }

    /**
     * Schedules {@code Jobs} which have a Scheduled annotation with cron expression.
     *
     * @throws SeedException when the {@code Job} can't be schedule.
     */
    private void scheduleJobs() {
        try {
            for (Class<?> candidateClass : jobClasses) {
                if (Task.class.isAssignableFrom(candidateClass)) {
                    Scheduled annotation = candidateClass.getAnnotation(Scheduled.class);
                    if (annotation != null && StringUtils.isNotBlank(annotation.value())) {
                        Class<? extends Task> taskClass = (Class<? extends Task>) candidateClass;
                        scheduledTasks.scheduledTask(taskClass).schedule();
                    }
                }
            }
        } catch (Exception e) {
            throw SeedException.wrap(e, SchedulerErrorCode.SCHEDULER_ERROR);
        }
    }

    @Override
    public void stop() {
        try {
            if (this.scheduler != null) {
                this.scheduler.shutdown();
            }
        } catch (SchedulerException e) {
            throw SeedException.wrap(e, SchedulerErrorCode.SCHEDULER_FAILED_TO_SHUTDOWN);
        }
        super.stop();
    }

    @Override
    public Object nativeUnitModule() {
        return new SchedulerModule(jobClasses, scheduler, jobListenerMap);
    }
}
