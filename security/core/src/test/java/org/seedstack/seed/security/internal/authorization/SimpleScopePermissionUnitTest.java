/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal.authorization;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.security.SimpleScope;

public class SimpleScopePermissionUnitTest {

    ScopePermission underTest;

    SimpleScope simpleScope;

    String permission;

    @Before
    public void before() {
        simpleScope = new SimpleScope("foo");
        permission = "bar";
        underTest = new ScopePermission(permission, simpleScope);
    }

    @Test
    public void test_constructor_with_string_permission() {
        underTest = new ScopePermission("bar", simpleScope);
        String perm = underTest.getPermission();
        assertEquals(perm, "bar");
        assertEquals(simpleScope, underTest.getScope());
    }
}
