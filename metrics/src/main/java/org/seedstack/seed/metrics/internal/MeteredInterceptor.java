/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Metered;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * A method interceptor which creates a meter for the declaring class with the given name (or the
 * method's name, if none was provided), and which measures the rate at which the annotated method
 * is invoked.
 */
final class MeteredInterceptor implements MethodInterceptor {
    private final Meter meter;

    private MeteredInterceptor(Meter meter) {
        this.meter = meter;
    }

    static MethodInterceptor forMethod(MetricRegistry metricRegistry, Class<?> klass, Method method) {
        final Metered annotation = method.getAnnotation(Metered.class);
        if (annotation != null) {
            final Meter meter = metricRegistry.meter(determineName(annotation, klass, method));
            return new MeteredInterceptor(meter);
        }
        return null;
    }

    private static String determineName(Metered annotation, Class<?> klass, Method method) {
        if (annotation.absolute()) {
            return annotation.name();
        }

        if (annotation.name().isEmpty()) {
            return MetricRegistry.name(klass, method.getName(), Meter.class.getSimpleName().toLowerCase());
        }

        return MetricRegistry.name(klass, annotation.name());
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        meter.mark();
        return invocation.proceed();
    }
}
