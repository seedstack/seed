/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.annotation.Counted;
import org.seedstack.seed.it.ITBind;

@ITBind
public class InstrumentedWithCounted {
    @Counted(name = "counted_things")
    public String doAThing() {
        return "poop";
    }

    @Counted(name = "monotonically_counted_things", monotonic = true)
    public String doAMonotonicThing() {
        return "poop";
    }

    @Counted
    String doAThingWithDefaultScope() {
        return "defaultResult";
    }

    @Counted
    protected String doAThingWithProtectedScope() {
        return "defaultProtected";
    }

    @Counted(name = "counted_absoluteName", absolute = true)
    protected String doAThingWithAbsoluteName() {
        return "defaultProtected";
    }
}
