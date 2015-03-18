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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.ExceptionMetered;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * A method interceptor which creates a meter for the declaring class with the given name (or the
 * method's name, if none was provided), and which measures the rate at which the annotated method
 * throws exceptions of a given type.
 *
 * @author yves.dautremay@mpsa.com
 */
final class ExceptionMeteredInterceptor implements MethodInterceptor {
    private final Meter meter;
    private final Class<? extends Throwable> klass;

    private ExceptionMeteredInterceptor(Meter meter, Class<? extends Throwable> klass) {
        this.meter = meter;
        this.klass = klass;
    }

    static MethodInterceptor forMethod(MetricRegistry metricRegistry, Class<?> klass, Method method) {
        final ExceptionMetered annotation = method.getAnnotation(ExceptionMetered.class);
        if (annotation != null) {
            final Meter meter = metricRegistry.meter(determineName(annotation, klass, method));
            return new ExceptionMeteredInterceptor(meter, annotation.cause());
        }
        return null;
    }

    private static String determineName(ExceptionMetered annotation, Class<?> klass, Method method) {
        if (annotation.absolute()) {
            return annotation.name();
        }

        if (annotation.name().isEmpty()) {
            return MetricRegistry.name(klass, method.getName(), ExceptionMetered.DEFAULT_NAME_SUFFIX);
        }

        return MetricRegistry.name(klass, annotation.name());
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (Throwable t) { // NOSONAR
            if (klass.isAssignableFrom(t.getClass())) {
                meter.mark();
            }
            throw t;
        }
    }
}
