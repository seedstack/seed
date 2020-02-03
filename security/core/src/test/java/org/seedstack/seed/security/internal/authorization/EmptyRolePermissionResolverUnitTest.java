/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
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
import org.seedstack.seed.security.Permission;
import org.seedstack.seed.security.Role;

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
