/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.MembersInjector;
import org.seedstack.shed.exception.SeedException;

import java.lang.reflect.Field;

/**
 * Guice members injector that inject metric instances.
 *
 * @param <T> The type of class to inject.
 * @author adrien.lauer@mpsa.com
 */
class MetricMembersInjector<T> implements MembersInjector<T> {
    private final MetricRegistry metricRegistry;
    private final String name;
    private final boolean absolute;
    private final Field field;

    MetricMembersInjector(MetricRegistry metricRegistry, Field field, String name, boolean absolute) {
        this.metricRegistry = metricRegistry;
        this.name = name;
        this.absolute = absolute;
        this.field = field;
        field.setAccessible(true);
    }

    @Override
    public void injectMembers(T t) {
        Object o;
        try {
            o = field.get(t);

            if (o != null) {
                if (o instanceof Metric) {
                    String fullName = determineName(field.getType().getSimpleName().toLowerCase());
                    try {
                        metricRegistry.register(fullName, (Metric) o);
                    } catch (IllegalArgumentException e) {
                        // re-inject metric from registry
                        field.set(t, metricRegistry.getMetrics().get(fullName));
                    }
                } else {
                    throw SeedException.createNew(MetricsErrorCode.INVALID_METRIC_TYPE)
                            .put("type", o.getClass().getName())
                            .put("field", field.getName())
                            .put("class", t.getClass().getName());
                }
            } else {
                Metric metric;

                if (Meter.class.isAssignableFrom(field.getType())) {
                    metric = metricRegistry.meter(determineName(Meter.class.getSimpleName().toLowerCase()));
                } else if (Timer.class.isAssignableFrom(field.getType())) {
                    metric = metricRegistry.timer(determineName(Timer.class.getSimpleName().toLowerCase()));
                } else if (Counter.class.isAssignableFrom(field.getType())) {
                    metric = metricRegistry.counter(determineName(Counter.class.getSimpleName().toLowerCase()));
                } else if (Histogram.class.isAssignableFrom(field.getType())) {
                    metric = metricRegistry.histogram(determineName(Histogram.class.getSimpleName().toLowerCase()));
                } else {
                    throw SeedException.createNew(MetricsErrorCode.INVALID_METRIC_TYPE)
                            .put("type", field.getType().getName())
                            .put("field", field.getName())
                            .put("class", t.getClass().getName());
                }

                field.set(t, metric);
            }
        } catch (IllegalAccessException e) {
            throw SeedException.wrap(e, MetricsErrorCode.ERROR_ACCESSING_METRIC_FIELD)
                    .put("class", field.getDeclaringClass().getCanonicalName())
                    .put("field", field.getName());
        }
    }

    private String determineName(String suffix) {
        if (absolute) {
            return name;
        }

        if (name.isEmpty()) {
            return MetricRegistry.name(field.getDeclaringClass(), field.getName(), suffix);
        }

        return MetricRegistry.name(field.getDeclaringClass(), name);
    }

}