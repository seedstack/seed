/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.google.inject.Injector;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedIT;

import javax.inject.Inject;

public class MetricsIT extends AbstractSeedIT {
    @Inject
    private Injector injector;

    @Test
    public void instrumented_classes_can_be_injected_multiple_times() throws Exception {
        for (int i = 0; i < 10; i++) {
            injector.getInstance(InstrumentedManually.class);
            injector.getInstance(InstrumentedWithCachedGauge.class);
            injector.getInstance(InstrumentedWithCounted.class);
            injector.getInstance(InstrumentedWithExceptionMetered.class);
            injector.getInstance(InstrumentedWithGauge.class);
            injector.getInstance(InstrumentedWithMetered.class);
            injector.getInstance(InstrumentedWithTimed.class);
        }
    }
}
