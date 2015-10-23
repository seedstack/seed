/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import org.apache.shiro.guice.ShiroModule;

/**
 * Guice module to initialize a default Shiro environment.
 *
 * @author yves.dautremay@mpsacom
 */
class DefaultShiroModule extends ShiroModule {
	@Override
	protected void configureShiro() {
		// nothing to do here
	}
}
