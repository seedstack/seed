/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.annotation.Timed;
import org.seedstack.seed.it.api.ITBind;

@ITBind
public class InstrumentedWithTimed {
    @Timed(name = "timed_things")
    public String doAThing() {
        return "poop";
    }

    @Timed
    String doAThingWithDefaultScope() {
        return "defaultResult";
    }

    @Timed
    protected String doAThingWithProtectedScope() {
        return "defaultProtected";
    }

    @Timed(name = "timed_absoluteName", absolute = true)
    protected String doAThingWithAbsoluteName() {
        return "defaultProtected";
    }
}
