/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Metric;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.reflect.Field;

/**
 * Guice type listener that will register any type having a field annotated with {@link com.codahale.metrics.annotation.Metric}.
 *
 * @author adrien.lauer@mpsa.com
 */
class MetricTypeListener implements TypeListener {
    private final MetricRegistry metricRegistry;

    MetricTypeListener(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
        for (Class<?> c = typeLiteral.getRawType(); c != Object.class; c = c.getSuperclass()) {
            for (Field field : typeLiteral.getRawType().getDeclaredFields()) {
                Metric metricAnnotation = field.getAnnotation(Metric.class);
                if (metricAnnotation != null && com.codahale.metrics.Metric.class.isAssignableFrom(field.getType())) {
                    typeEncounter.register(new MetricMembersInjector<>(metricRegistry, field, metricAnnotation.name(), metricAnnotation.absolute()));
                }
            }
        }
    }
}
