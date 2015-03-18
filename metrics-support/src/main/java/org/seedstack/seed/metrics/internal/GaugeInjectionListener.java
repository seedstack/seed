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

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.spi.InjectionListener;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.metrics.api.MetricsErrorCode;

import java.lang.reflect.Method;

/**
 * An injection listener which creates a gauge for the declaring class with the given name (or the
 * method's name, if none was provided) which returns the value returned by the annotated method.
 *
 * @author yves.dautremay@mpsa.com
 */
class GaugeInjectionListener<I> implements InjectionListener<I> {
    private final MetricRegistry metricRegistry;
    private final String metricName;
    private final Method method;

    GaugeInjectionListener(MetricRegistry metricRegistry, String metricName, Method method) {
        this.metricRegistry = metricRegistry;
        this.metricName = metricName;
        this.method = method;
    }

    @Override
    public void afterInjection(final I i) {
        try {
            metricRegistry.register(metricName, new Gauge<Object>() {
                @Override
                public Object getValue() {
                    try { // NOSONAR
                        return method.invoke(i);
                    } catch (Exception e) {
                        throw SeedException.wrap(e, MetricsErrorCode.ERROR_EVALUATING_METRIC);
                    }
                }
            });
        } catch (IllegalArgumentException e) { // NOSONAR
            // ignore because metrics should be registered only once
        }
    }
}
