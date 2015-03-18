/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.internal;

import com.google.inject.name.Names;
import org.apache.shiro.guice.ShiroModule;
import org.seedstack.seed.security.spi.SecurityConcern;


/**
 * Guice module to initialize a Shiro environment for WS entry point.
 *
 * @author yves.dautremay@mpsa.com
 */
@SecurityConcern
class WSSecurityModule extends ShiroModule {
    private static final String WS_SECURITY_MANAGER_NAME = "wsSecurityManager";

	@Override
	protected void configureShiro() {
		bind(org.apache.shiro.mgt.SecurityManager.class).annotatedWith(Names.named(WS_SECURITY_MANAGER_NAME)).to(org.apache.shiro.mgt.SecurityManager.class);
		expose(org.apache.shiro.mgt.SecurityManager.class).annotatedWith(Names.named(WS_SECURITY_MANAGER_NAME));
	}
}
