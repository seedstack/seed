/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.fixtures;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.seedstack.seed.Install;

@Install(override = true)
class OverridingModule3 extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(Service.class, Names.named("OverridingNothing"))).to(DummyService3.class);
        bind(Key.get(Service.class, Names.named("Overriding"))).to(DummyService2.class);
    }

}
