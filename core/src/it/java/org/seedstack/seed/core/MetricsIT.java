/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.seedstack.seed.core.internal.dependency.DependencyProxy;
import org.seedstack.seed.core.internal.metrics.MetricsProvider;
import org.seedstack.seed.core.rules.SeedITRule;

import javax.inject.Inject;
import java.util.Optional;

public class MetricsIT {
    @Rule
    public SeedITRule rule = new SeedITRule(this);
    private Injector injector;

    private static class MyObjectWithMetrics {
        private static final long GAUGE_VALUE = 4L;
        static final String NEW_GAUGE = "new-gauge";
        static final String NEW_METRIC = "new-metric";
        @Inject
        Optional<MetricsProvider> metricsProvider;

        void start() {
            if (metricsProvider.isPresent()) {
                final Counter c = new Counter();
                metricsProvider.get().register(NEW_METRIC, () -> c);
                c.inc();

                DependencyProxy<Gauge<Long>> gauge = new DependencyProxy<>(new Class[]{Gauge.class}, new Object() {
                    @SuppressWarnings("unused")
                    public Long getValue() {
                        return GAUGE_VALUE;
                    }
                });

                metricsProvider.get().register(NEW_GAUGE, gauge.getProxy());

            }
        }
    }

    @Before
    public void before() {
        injector = rule.getKernel().objectGraph().as(Injector.class).createChildInjector((Module) binder -> binder.bind(MyObjectWithMetrics.class));
    }

    /**
     * Test metrics are added if metrics is in the classpath
     */
    @Test
    public void test() {
        MyObjectWithMetrics o = injector.getInstance(MyObjectWithMetrics.class);
        o.start();
        Optional<MetricsProvider> provider = injector.getInstance(Key.get(new TypeLiteral<Optional<MetricsProvider>>() {
        }));
        Assertions.assertThat(provider.isPresent()).isTrue();
        MetricRegistry metricRegistry = provider.get().getMetricRegistry();
        Assertions.assertThat(metricRegistry.counter(MyObjectWithMetrics.NEW_METRIC)).isNotNull();
        Counter c = metricRegistry.counter(MyObjectWithMetrics.NEW_METRIC);
        Assertions.assertThat(c.getCount()).isEqualTo(1);
        Assertions.assertThat(metricRegistry.getGauges().containsKey(MyObjectWithMetrics.NEW_GAUGE)).isNotNull();
        @SuppressWarnings("unchecked")
        Gauge<Long> gauge = metricRegistry.getGauges().get(MyObjectWithMetrics.NEW_GAUGE);
        Assertions.assertThat(gauge.getValue()).isEqualTo(MyObjectWithMetrics.GAUGE_VALUE);

    }

}
