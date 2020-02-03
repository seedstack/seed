/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.authorization;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.fest.reflect.core.Reflection;
import org.fest.reflect.reference.TypeRef;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.security.Role;
import org.seedstack.seed.security.SecurityConfig;

public class ConfigurationRolePermissionResolverUnitTest {

    ConfigurationRolePermissionResolver underTest;

    @Before
    public void before() {
        underTest = new ConfigurationRolePermissionResolver();
    }

    @Test
    public void resolvePermissionsInRole_should_return_permissions() {
        Map<String, Set<String>> rolesMap = Reflection.field("roles").ofType(new TypeRef<Map<String, Set<String>>>() {
        }).in(underTest).get();
        Set<String> permissions = new HashSet<>();
        permissions.add("bar");
        rolesMap.put("foo", permissions);

        underTest.resolvePermissionsInRole(new Role("foo"));
    }

    @Test
    public void readConfiguration_should_build_roles() {
        SecurityConfig securityConfig = new SecurityConfig().addRolePermissions("foo",
                Sets.newHashSet("bar", "foobar"));
        underTest.readConfiguration(securityConfig);
        Map<String, Set<String>> map = Reflection.field("roles").ofType(new TypeRef<Map<String, Set<String>>>() {
        }).in(underTest).get();
        Set<String> permissions = map.get("foo");
        assertTrue(permissions.contains("bar"));
        assertTrue(permissions.contains("foobar"));
    }
}
