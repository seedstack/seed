/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.seedstack.seed.it.AbstractSeedIT;
import org.seedstack.seed.it.KernelMode;
import org.seedstack.seed.it.spi.ITKernelMode;
import org.junit.Test;

import javax.inject.Inject;

import static com.codahale.metrics.MetricRegistry.name;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@KernelMode(ITKernelMode.PER_TEST)
public class MeteredIT extends AbstractSeedIT {
    @Inject
    private InstrumentedWithMetered instance;

    @Inject
    private MetricRegistry registry;

    @Test
    public void aMeteredAnnotatedMethod() throws Exception {

        instance.doAThing();

        final Meter metric = registry.getMeters().get(name(InstrumentedWithMetered.class, "metered_things"));

        assertMetricIsSetup(metric);

        assertThat("Guice creates a meter which gets marked",
                metric.getCount(),
                is(1L));
    }

    @Test
    public void aMeteredAnnotatedMethodWithDefaultScope() throws Exception {

        final Meter metric = registry.getMeters().get(name(InstrumentedWithMetered.class, "doAThingWithDefaultScope", Meter.class.getSimpleName().toLowerCase()));
        assertMetricIsSetup(metric);

        assertThat("Metric initialises to zero",
                metric.getCount(),
                is(0L));

        instance.doAThingWithDefaultScope();

        assertThat("Metric is marked",
                metric.getCount(),
                is(1L));
    }

    @Test
    public void aMeteredAnnotatedMethodWithProtectedScope() throws Exception {

        final Meter metric = registry.getMeters().get(name(InstrumentedWithMetered.class, "doAThingWithProtectedScope", Meter.class.getSimpleName().toLowerCase()));

        assertMetricIsSetup(metric);

        assertThat("Metric initialises to zero",
                metric.getCount(),
                is(0L));

        instance.doAThingWithProtectedScope();

        assertThat("Metric is marked",
                metric.getCount(),
                is(1L));
    }

    @Test
    public void aMeteredAnnotatedMethodWithName() throws Exception {

        final Meter metric = registry.getMeters().get(name(InstrumentedWithMetered.class, "metered_n"));

        assertMetricIsSetup(metric);

        assertThat("Metric initialises to zero",
                metric.getCount(),
                is(0L));

        instance.doAThingWithName();

        assertThat("Metric is marked",
                metric.getCount(),
                is(1L));
    }

    @Test
    public void aMeteredAnnotatedMethodWithAbsoluteName() {
        final Meter metric = registry.getMeters().get(name("metered_nameAbs"));

        assertMetricIsSetup(metric);

        assertThat("Metric initialises to zero",
                metric.getCount(),
                is(0L));

        instance.doAThingWithAbsoluteName();

        assertThat("Metric is marked",
                metric.getCount(),
                is(1L));
    }

    private void assertMetricIsSetup(final Meter metric) {
        assertThat("Guice creates a metric",
                metric,
                is(notNullValue()));
    }
}
