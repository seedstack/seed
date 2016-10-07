/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Counted;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * A method interceptor which creates a counter for the declaring class with the given name (or the
 * method's name, if none was provided), and which count the execution of the annotated method.
 */
final class CountedInterceptor implements MethodInterceptor {
    private final Counter counter;
    private final boolean monotonic;

    private CountedInterceptor(Counter counter, boolean monotonic) {
        this.counter = counter;
        this.monotonic = monotonic;
    }

    static MethodInterceptor forMethod(MetricRegistry metricRegistry, Class<?> klass, Method method) {
        final Counted annotation = method.getAnnotation(Counted.class);
        if (annotation != null) {
            final Counter timer = metricRegistry.counter(determineName(annotation, klass, method));
            return new CountedInterceptor(timer, annotation.monotonic());
        }
        return null;
    }

    private static String determineName(Counted annotation, Class<?> klass, Method method) {
        if (annotation.absolute()) {
            return annotation.name();
        }

        if (annotation.name().isEmpty()) {
            return MetricRegistry.name(klass, method.getName(), Counter.class.getSimpleName().toLowerCase());
        }

        return MetricRegistry.name(klass, annotation.name());
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        counter.inc();
        try {
            return invocation.proceed();
        } finally {
            if (!monotonic) {
                counter.dec();
            }
        }
    }
}
