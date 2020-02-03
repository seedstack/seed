/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.seedstack.seed.security.Realm;
import org.seedstack.seed.security.RoleMapping;
import org.seedstack.seed.security.RolePermissionResolver;
import org.seedstack.seed.security.internal.authorization.ConfigurationRoleMapping;
import org.seedstack.seed.security.internal.authorization.EmptyRolePermissionResolver;
import org.seedstack.seed.security.internal.realms.ConfigurationRealm;

public class ConfigurationRealmUnitTest {

    RealmConfiguration underTest;

    @Test
    public void test_constructor_and_setters() {
        final String name = "foo";
        final Class<? extends Realm> realmClass = ConfigurationRealm.class;
        underTest = new RealmConfiguration(name, realmClass);
        assertEquals(name, underTest.getName());
        assertEquals(realmClass, underTest.getRealmClass());

        final Class<? extends RoleMapping> roleMappingClass = ConfigurationRoleMapping.class;
        final Class<? extends RolePermissionResolver> rolePermissionResolverClass = EmptyRolePermissionResolver.class;
        underTest.setRoleMappingClass(roleMappingClass);
        underTest.setRolePermissionResolverClass(rolePermissionResolverClass);
        assertEquals(roleMappingClass, underTest.getRoleMappingClass());
        assertEquals(rolePermissionResolverClass, underTest.getRolePermissionResolverClass());
    }
}
