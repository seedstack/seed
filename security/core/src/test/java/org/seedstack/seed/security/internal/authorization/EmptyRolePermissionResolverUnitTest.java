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

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import org.seedstack.seed.security.api.Permission;
import org.seedstack.seed.security.api.Role;

public class EmptyRolePermissionResolverUnitTest {

	EmptyRolePermissionResolver underTest;

	@Before
	public void before() {
		underTest = new EmptyRolePermissionResolver();
	}

	@Test
	public void resolvePermission_should_return_empty_list() {
		Role role = new Role("foo");
		Collection<Permission> permissions = underTest.resolvePermissionsInRole(role);
		assertTrue(permissions.isEmpty());
	}
}
