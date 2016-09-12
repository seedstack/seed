/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.CachedGauge;
import com.codahale.metrics.annotation.Gauge;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.reflect.Method;

/**
 * A listener which adds gauge injection listeners to classes with gauges.
 *
 * @author yves.dautremay@mpsa.com
 */
class GaugeListener implements TypeListener {
    private final MetricRegistry metricRegistry;

    GaugeListener(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    private static String determineName(Gauge annotation, Class<?> klass, Method method) {
        if (annotation.absolute()) {
            return annotation.name();
        }

        if (annotation.name().isEmpty()) {
            return MetricRegistry.name(klass, method.getName(), com.codahale.metrics.Gauge.class.getSimpleName().toLowerCase());
        }

        return MetricRegistry.name(klass, annotation.name());
    }

    private static String determineName(CachedGauge annotation, Class<?> klass, Method method) {
        if (annotation.absolute()) {
            return annotation.name();
        }

        if (annotation.name().isEmpty()) {
            return MetricRegistry.name(klass, method.getName(), com.codahale.metrics.Gauge.class.getSimpleName().toLowerCase());
        }

        return MetricRegistry.name(klass, annotation.name());
    }

    @Override
    public <I> void hear(final TypeLiteral<I> literal, TypeEncounter<I> encounter) {
        Class<? super I> klass = literal.getRawType();
        for (final Method method : klass.getMethods()) {
            final Gauge gaugeAnnotation = method.getAnnotation(Gauge.class);
            if (gaugeAnnotation != null) {
                if (method.getParameterTypes().length == 0) {
                    final String metricName = determineName(gaugeAnnotation, klass, method);
                    encounter.register(new GaugeInjectionListener<>(metricRegistry,
                            metricName,
                            method));
                } else {
                    encounter.addError("Method %s is annotated with @Gauge but requires parameters.",
                            method);
                }
            }

            final CachedGauge cachedGaugeAnnotation = method.getAnnotation(CachedGauge.class);
            if (cachedGaugeAnnotation != null) {
                if (method.getParameterTypes().length == 0) {
                    final String metricName = determineName(cachedGaugeAnnotation, klass, method);
                    encounter.register(new CachedGaugeInjectionListener<>(metricRegistry,
                            metricName,
                            method,
                            cachedGaugeAnnotation.timeout(),
                            cachedGaugeAnnotation.timeoutUnit()));
                } else {
                    encounter.addError("Method %s is annotated with @Gauge but requires parameters.",
                            method);
                }
            }
        }
    }

}
