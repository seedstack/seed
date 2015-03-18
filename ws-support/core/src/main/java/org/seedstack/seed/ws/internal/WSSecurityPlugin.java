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

import com.google.inject.Module;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequestBuilder;
import org.apache.shiro.guice.ShiroModule;
import org.seedstack.seed.security.internal.SeedSecurityPlugin;

import java.util.Collection;
import java.util.Collections;

/**
 * This security plugin provides security to WS endpoints.
 *
 * @author yves.dautremay@mpsa.com
 */
public class WSSecurityPlugin implements SeedSecurityPlugin {

	@Override
	public void init(InitContext initContext) {
        // nothing to do here
	}

	@Override
	public void provideContainerContext(Object containerContext) {
        // nothing to do here
	}

	@Override
	public void classpathScanRequests(ClasspathScanRequestBuilder classpathScanRequestBuilder) {
        // nothing to do here
	}

	@Override
	public ShiroModule provideShiroModule() {
		return new WSSecurityModule();
	}

	@Override
	public Collection<Module> provideOtherModules() {
		return Collections.emptyList();
	}
}