/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import org.apache.shiro.guice.ShiroModule;
import org.apache.shiro.mgt.SecurityManager;

import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * Guice module to initialize a default Shiro environment.
 *
 * @author yves.dautremay@mpsa.com
 */
class DefaultShiroModule extends ShiroModule {
    private static final String DEFAULT_SECURITY_MANAGER_NAME = "defaultSecurityManager";

    @Override
	protected void configureShiro() {
		bind(SecurityManager.class).annotatedWith(Names.named(DEFAULT_SECURITY_MANAGER_NAME)).to(Key.get(SecurityManager.class));
		expose(SecurityManager.class).annotatedWith(Names.named(DEFAULT_SECURITY_MANAGER_NAME));
	}
}
