/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.Gauge;
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
public class GaugeIT extends AbstractSeedIT {
    @Inject
    private InstrumentedWithGauge instance;

    @Inject
    private MetricRegistry registry;

    @Test
    @SuppressWarnings("unchecked")
    public void aGaugeAnnotatedMethod() throws Exception {
        instance.doAThing();

        final Gauge metric = registry.getGauges().get(name(InstrumentedWithGauge.class, "gauge_things"));

        assertThat("Guice creates a metric",
                metric,
                is(notNullValue()));

        assertThat("Guice creates a gauge with the given value",
                ((Gauge<String>) metric).getValue(),
                is("poop"));
    }


    @Test
    @SuppressWarnings("unchecked")
    public void aGaugeAnnotatedMethodWithDefaultName() throws Exception {
        instance.doAnotherThing();

        final Gauge metric = registry.getGauges().get(name(InstrumentedWithGauge.class,
                "doAnotherThing", Gauge.class.getSimpleName().toLowerCase()));

        assertThat("Guice creates a metric",
                metric,
                is(notNullValue()));

        assertThat("Guice creates a gauge with the given value",
                ((Gauge<String>) metric).getValue(),
                is("anotherThing"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void aGaugeAnnotatedMethodWithAbsoluteName() throws Exception {
        instance.doAThingWithAbsoluteName();

        final Gauge metric = registry.getGauges().get(name("gauge_absoluteName"));

        assertThat("Guice creates a metric",
                metric,
                is(notNullValue()));

        assertThat("Guice creates a gauge with the given value",
                ((Gauge<String>) metric).getValue(),
                is("anotherThingWithAbsoluteName"));
    }

}
