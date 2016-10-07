/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.spi.InjectionListener;
import org.seedstack.seed.SeedException;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * An injection listener which creates a gauge for the declaring class with the given name (or the
 * method's name, if none was provided) which returns the value returned by the annotated method.
 */
class CachedGaugeInjectionListener<I> implements InjectionListener<I> {
    private final MetricRegistry metricRegistry;
    private final String metricName;
    private final Method method;
    private final long timeout;
    private final TimeUnit timeUnit;

    CachedGaugeInjectionListener(MetricRegistry metricRegistry, String metricName, Method method, long timeout, TimeUnit timeUnit) {
        this.metricRegistry = metricRegistry;
        this.metricName = metricName;
        this.method = method;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    @Override
    public void afterInjection(final I i) {
        try {
            metricRegistry.register(metricName, new CachedGauge<Object>(timeout, timeUnit) {
                @Override
                protected Object loadValue() {
                    try {
                        return method.invoke(i);
                    } catch (Exception e) {
                        throw SeedException.wrap(e, MetricsErrorCode.ERROR_EVALUATING_METRIC);
                    }
                }
            });
        } catch (IllegalArgumentException e) {
            // ignore because metrics should be registered only once
        }
    }
}
