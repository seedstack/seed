/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import java.util.Set;

import org.seedstack.seed.metrics.HealthChecked;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.MapBinder;

class MetricsModule extends AbstractModule {
    private final MetricRegistry metricRegistry;
    private final HealthCheckRegistry healthCheckRegistry;
    private final Set<Class<? extends HealthCheck>> healthCheckClasses;

    MetricsModule(MetricRegistry metricRegistry, HealthCheckRegistry healthCheckRegistry, Set<Class<? extends HealthCheck>> healthCheckClasses) {
        this.metricRegistry = metricRegistry;
        this.healthCheckRegistry = healthCheckRegistry;
        this.healthCheckClasses = healthCheckClasses;
    }

    @Override
    protected void configure() {
        bind(MetricRegistry.class).toInstance(metricRegistry);
        bind(HealthCheckRegistry.class).toInstance(healthCheckRegistry);

        MapBinder<String, HealthCheck> multiBinder = MapBinder.newMapBinder(binder(), String.class, HealthCheck.class);
        for (Class<? extends HealthCheck> healthCheckClass : healthCheckClasses) {
        	if ( null == healthCheckClass.getAnnotation(HealthChecked.class) || healthCheckClass.getAnnotation(HealthChecked.class).name().isEmpty()) {
                multiBinder.addBinding(healthCheckClass.getCanonicalName()).to(healthCheckClass);
        	}else{
        		multiBinder.addBinding(healthCheckClass.getAnnotation(HealthChecked.class).name()).to(healthCheckClass);
        	}
        }

        bindListener(Matchers.any(), new MetricTypeListener(metricRegistry));
        bindListener(Matchers.any(), new MeteredListener(metricRegistry));
        bindListener(Matchers.any(), new TimedListener(metricRegistry));
        bindListener(Matchers.any(), new GaugeListener(metricRegistry));
        bindListener(Matchers.any(), new ExceptionMeteredListener(metricRegistry));
        bindListener(Matchers.any(), new CountedListener(metricRegistry));
    }
}
