/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Timed;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * A method interceptor which creates a timer for the declaring class with the given name (or the
 * method's name, if none was provided), and which times the execution of the annotated method.
 *
 * @author yves.dautremay@mpsa.com
 */
final class TimedInterceptor implements MethodInterceptor {
    private final Timer timer;

    private TimedInterceptor(Timer timer) {
        this.timer = timer;
    }

    static MethodInterceptor forMethod(MetricRegistry metricRegistry, Class<?> klass, Method method) {
        final Timed annotation = method.getAnnotation(Timed.class);
        if (annotation != null) {
            final Timer timer = metricRegistry.timer(determineName(annotation, klass, method));
            return new TimedInterceptor(timer);
        }
        return null;
    }

    private static String determineName(Timed annotation, Class<?> klass, Method method) {
        if (annotation.absolute()) {
            return annotation.name();
        }

        if (annotation.name().isEmpty()) {
            return MetricRegistry.name(klass, method.getName(), Timer.class.getSimpleName().toLowerCase());
        }

        return MetricRegistry.name(klass, annotation.name());
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final Timer.Context ctx = timer.time();
        try {
            return invocation.proceed();
        } finally {
            ctx.stop();
        }
    }
}
