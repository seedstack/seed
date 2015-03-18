/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it.internal;


import io.nuun.kernel.core.AbstractPlugin;

/**
 * This plugin automatically enable integration tests to be managed by SEED.
 *
 * @author redouane.loulou@ext.mpsa.com
 */
public class SeedWebITPlugin extends AbstractPlugin {
	@Override
	public String name() {
		return "seed-integrationtest-web-plugin";
	}

	@Override
	public Object nativeUnitModule() {
		return new SeedWebITModule();
	}

}
