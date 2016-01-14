/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.annotation.Gauge;
import org.seedstack.seed.it.ITBind;

@ITBind
public class InstrumentedWithGauge {
    @Gauge(name = "gauge_things")
    public String doAThing() {
        return "poop";
    }

    @Gauge
    public String doAnotherThing() {
        return "anotherThing";
    }

    @Gauge(name = "gauge_absoluteName", absolute = true)
    public String doAThingWithAbsoluteName() {
        return "anotherThingWithAbsoluteName";
    }
}
