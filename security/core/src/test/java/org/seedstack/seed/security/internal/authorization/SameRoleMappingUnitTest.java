/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal.authorization;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.security.Role;

public class SameRoleMappingUnitTest {

    SameRoleMapping underTest;

    @Before
    public void before() {
        underTest = new SameRoleMapping();
    }

    @Test
    public void resolveRoles_should_return_given_role() {
        final String role = "foo";
        Set<String> foos = new HashSet<>();
        foos.add(role);
        Set<Role> roles = underTest.resolveRoles(foos, null);
        assertTrue(roles.size() == 1);
        assertTrue(roles.contains(new Role(role)));
    }
}
