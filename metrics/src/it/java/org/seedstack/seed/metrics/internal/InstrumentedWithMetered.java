/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.annotation.Metered;
import org.seedstack.seed.it.ITBind;

@ITBind
public class InstrumentedWithMetered {
    @Metered(name = "metered_things")
    public String doAThing() {
        return "poop";
    }

    @Metered
    String doAThingWithDefaultScope() {
        return "defaultResult";
    }

    @Metered
    protected String doAThingWithProtectedScope() {
        return "defaultProtected";
    }

    @Metered(name = "metered_n")
    protected String doAThingWithName() {
        return "withName";
    }


    @Metered(name = "metered_nameAbs", absolute = true)
    protected String doAThingWithAbsoluteName() {
        return "absoluteName";
    }
}
