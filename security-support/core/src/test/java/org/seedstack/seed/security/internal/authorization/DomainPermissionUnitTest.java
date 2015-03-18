/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.authorization;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import org.seedstack.seed.security.api.Domain;

public class DomainPermissionUnitTest {

	ScopePermission underTest;
	
	Domain domain;
	
	String permission;
	
	@Before
	public void before(){
		domain = new Domain("foo");
		permission = "bar";
		underTest = new ScopePermission(permission, domain);
	}
	
	@Test
	public void test_constructor_with_string_permission(){
		underTest = new ScopePermission("bar", domain);
		String perm = underTest.getPermission();
		assertEquals(perm, "bar");
		assertEquals(domain, underTest.getScope());
	}
}
