/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedIT;
import org.seedstack.seed.it.KernelMode;
import org.seedstack.seed.it.spi.ITKernelMode;

import javax.inject.Inject;

import static com.codahale.metrics.MetricRegistry.name;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@KernelMode(ITKernelMode.PER_TEST)
public class CountedIT extends AbstractSeedIT {
    @Inject
    private InstrumentedWithCounted instance;

    @Inject
    private MetricRegistry registry;

    @Test
    public void aCountedAnnotatedMethod() throws Exception {

        instance.doAThing();

        final Counter metric = registry.getCounters().get(name(InstrumentedWithCounted.class,
                "counted_things"));

        assertMetricSetup(metric);

        assertThat("Guice creates a counter which records invocation count",
                metric.getCount(),
                is(0L));
    }

    @Test
    public void aMonotonicallyCountedAnnotatedMethod() throws Exception {

        instance.doAMonotonicThing();

        final Counter metric = registry.getCounters().get(name(InstrumentedWithCounted.class,
                "monotonically_counted_things"));

        assertMetricSetup(metric);

        assertThat("Guice creates a counter which records invocation count",
                metric.getCount(),
                is(1L));
    }

    @Test
    public void aCountedAnnotatedMethodWithDefaultScope() throws Exception {

        instance.doAThingWithDefaultScope();

        final Counter metric = registry.getCounters().get(name(InstrumentedWithCounted.class,
                "doAThingWithDefaultScope", Counter.class.getSimpleName().toLowerCase()));

        assertMetricSetup(metric);
    }

    @Test
    public void aCountedAnnotatedMethodWithProtectedScope() throws Exception {

        instance.doAThingWithProtectedScope();

        final Counter metric = registry.getCounters().get(name(InstrumentedWithCounted.class,
                "doAThingWithProtectedScope", Counter.class.getSimpleName().toLowerCase()));

        assertMetricSetup(metric);
    }

    @Test
    public void aCountedAnnotatedMethodWithAbsoluteName() throws Exception {

        instance.doAThingWithAbsoluteName();

        final Counter metric = registry.getCounters().get(name("counted_absoluteName"));

        assertMetricSetup(metric);
    }

    private void assertMetricSetup(final Counter metric) {
        assertThat("Guice creates a metric",
                metric,
                is(notNullValue()));
    }
}
