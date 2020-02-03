/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.fixtures;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

// Module is not installed for test needs
class Module2 extends AbstractModule {

    @Override
    protected void configure() {
        bind(Service2.class).to(DummyService2.class).in(Scopes.SINGLETON);
        bind(Key.get(Service.class, Names.named("Service2"))).to(Key.get(Service1.class));
    }

}
