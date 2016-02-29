/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedIT;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricIT extends AbstractSeedIT {
    @Inject
    private InstrumentedManually instance;

    @Inject
    private MetricRegistry registry;

    @Inject
    private Injector injector;

    @Test
    public void metrics_are_correctly_injected() throws Exception {
        assertThat(instance.getCounter()).isNotNull();
        assertThat(instance.getHistogram()).isNotNull();
        assertThat(instance.getHistogram2()).isNotNull();
        assertThat(instance.getMeter()).isNotNull();
        assertThat(instance.getTimer()).isNotNull();
    }

    @Test
    public void metrics_are_correctly_registered() throws Exception {
        assertThat(registry.getCounters().get("org.seedstack.seed.metrics.internal.InstrumentedManually.counter.counter")).isNotNull();
        assertThat(registry.getHistograms().get("org.seedstack.seed.metrics.internal.InstrumentedManually.histogram.histogram")).isNotNull();
        assertThat(registry.getHistograms().get("org.seedstack.seed.metrics.internal.InstrumentedManually.histogram2.histogram")).isNotNull();
        assertThat(registry.getMeters().get("org.seedstack.seed.metrics.internal.InstrumentedManually.meter.meter")).isNotNull();
        assertThat(registry.getTimers().get("org.seedstack.seed.metrics.internal.InstrumentedManually.timer.timer")).isNotNull();
    }

    @Test
    public void metrics_are_preserved_between_instances() throws Exception {
        InstrumentedManually instrumentedManually1 = injector.getInstance(InstrumentedManually.class);
        assertThat(instrumentedManually1.getCounter().getCount()).isEqualTo(0);
        instrumentedManually1.getCounter().inc();
        assertThat(instrumentedManually1.getCounter().getCount()).isEqualTo(1);

        InstrumentedManually instrumentedManually2 = injector.getInstance(InstrumentedManually.class);
        assertThat(instrumentedManually1.getCounter().getCount()).isEqualTo(1);
        instrumentedManually1.getCounter().inc();
        assertThat(instrumentedManually1.getCounter().getCount()).isEqualTo(2);
    }
}
