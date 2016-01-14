/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.fixtures;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import org.seedstack.seed.Install;

@Install
class Module3 extends AbstractModule {

	@Override
	protected void configure() {
		bind(Service3.class).toProvider(Providers.<Service3>of(null));
        bind(Key.get(Service.class, Names.named("Service3"))).to(Key.get(Service1.class));
	}

}
