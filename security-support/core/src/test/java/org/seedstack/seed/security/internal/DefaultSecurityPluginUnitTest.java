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

import com.google.inject.Module;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequestBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class DefaultSecurityPluginUnitTest {

	private DefaultSecurityPlugin underTest;
	
	@Before
	public void before(){
		underTest = new DefaultSecurityPlugin();
	}
	
	@Test
	public void init_does_nothing(){
		InitContext context = mock(InitContext.class);
		verifyZeroInteractions(context);
		underTest.init(context);
	}
	
	@Test
	public void classPathScanRequest_does_nothing(){
		ClasspathScanRequestBuilder builder = mock(ClasspathScanRequestBuilder.class);
		verifyZeroInteractions(builder);
		underTest.classpathScanRequests(builder);
	}
	
	@Test
	public void provideContainerContext_does_nothing(){
		Object containerContext = mock(Object.class);
		verifyZeroInteractions(containerContext);
		underTest.provideContainerContext(containerContext);
	}
	
	@Test
	public void provideModules_provides_modules(){
		Collection<Module> modules = underTest.provideOtherModules();
		assertTrue(modules.isEmpty());
	}
	
	@Test
	public void provideShiroModule_provides_shiro_module(){
		Module m = underTest.provideShiroModule();
		assertEquals(DefaultShiroModule.class, m.getClass());
	}
}
