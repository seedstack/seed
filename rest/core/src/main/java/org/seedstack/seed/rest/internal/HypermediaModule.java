/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import com.google.inject.AbstractModule;
import org.seedstack.seed.rest.RelRegistry;
import org.seedstack.seed.rest.internal.jsonhome.JsonHome;

class HypermediaModule extends AbstractModule {
    private final JsonHome jsonHome;
    private final RelRegistry relRegistry;

    HypermediaModule(JsonHome jsonHome, RelRegistry relRegistry) {
        this.jsonHome = jsonHome;
        this.relRegistry = relRegistry;
    }

    @Override
    protected void configure() {
        bind(JsonHome.class).toInstance(jsonHome);
        bind(RelRegistry.class).toInstance(relRegistry);
    }
}
