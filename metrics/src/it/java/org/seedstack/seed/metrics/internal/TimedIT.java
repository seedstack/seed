/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
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
public class TimedIT extends AbstractSeedIT {
    @Inject
    private InstrumentedWithTimed instance;

    @Inject
    private MetricRegistry registry;

    @Test
    public void aTimedAnnotatedMethod() throws Exception {

        instance.doAThing();

        final Timer metric = registry.getTimers().get(name(InstrumentedWithTimed.class,
                "timed_things"));

        assertMetricSetup(metric);

        assertThat("Guice creates a timer which records invocation length",
                metric.getCount(),
                is(1L));
    }

    @Test
    public void aTimedAnnotatedMethodWithDefaultScope() throws Exception {

        instance.doAThingWithDefaultScope();

        final Timer metric = registry.getTimers().get(name(InstrumentedWithTimed.class,
                "doAThingWithDefaultScope", Timer.class.getSimpleName().toLowerCase()));

        assertMetricSetup(metric);
    }

    @Test
    public void aTimedAnnotatedMethodWithProtectedScope() throws Exception {

        instance.doAThingWithProtectedScope();

        final Timer metric = registry.getTimers().get(name(InstrumentedWithTimed.class,
                "doAThingWithProtectedScope", Timer.class.getSimpleName().toLowerCase()));

        assertMetricSetup(metric);
    }

    @Test
    public void aTimedAnnotatedMethodWithAbsoluteName() throws Exception {

        instance.doAThingWithAbsoluteName();

        final Timer metric = registry.getTimers().get(name("timed_absoluteName"));

        assertMetricSetup(metric);
    }

    private void assertMetricSetup(final Timer metric) {
        assertThat("Guice creates a metric",
                metric,
                is(notNullValue()));
    }
}
