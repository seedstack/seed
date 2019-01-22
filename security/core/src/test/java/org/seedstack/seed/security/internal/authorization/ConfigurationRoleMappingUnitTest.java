/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.authorization;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.fest.reflect.core.Reflection;
import org.fest.reflect.reference.TypeRef;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.security.Role;
import org.seedstack.seed.security.Scope;
import org.seedstack.seed.security.SecurityConfig;

public class ConfigurationRoleMappingUnitTest {

    ConfigurationRoleMapping underTest;
    private String role = "foo";
    private String mappedRole1 = "bar";
    private String mappedRole2 = "foobar";
    private Map<String, Set<String>> map = new HashMap<>();

    @Before
    public void before() {
        underTest = new ConfigurationRoleMapping();
        Set<String> mappedRoles = new HashSet<>();
        mappedRoles.add(mappedRole1);
        mappedRoles.add(mappedRole2);
        map.put(role, mappedRoles);
        Reflection.field("map").ofType(new TypeRef<Map<String, Set<String>>>() {
        }).in(underTest).set(map);
        Reflection.field("scopeClasses").ofType(new TypeRef<Map<String, Class<? extends Scope>>>() {
        }).in(underTest).set(new HashMap<>());
    }

    @SuppressWarnings("serial")
    @Test
    public void resolveRoles_should_return_mapped_role() {
        Set<String> set1 = new HashSet<>();
        set1.add(role);
        Collection<Role> resolvedRoles = underTest.resolveRoles(set1, null);
        assertTrue(resolvedRoles.contains(new Role(mappedRole1)));
        assertTrue(resolvedRoles.contains(new Role(mappedRole2)));

        Set<String> set2 = new HashSet<>();
        set2.add("toto");
        Collection<Role> noRoles = underTest.resolveRoles(new HashSet<String>() {{
            add("toto");
        }}, null);
        assertTrue(noRoles.isEmpty());
    }

    @Test
    public void readConfiguration_should_fill_map() {
        SecurityConfig securityConfig = new SecurityConfig()
                .addRole("foo", Sets.newHashSet("bar.foo", "foo.bar"));
        underTest.readConfiguration(securityConfig);
        Map<String, Set<String>> roleMap = Reflection.field("map").ofType(new TypeRef<Map<String, Set<String>>>() {
        }).in(underTest).get();
        Set<String> roles = roleMap.get("bar.foo");
        assertTrue(roles.contains("foo"));

        Set<String> roles2 = roleMap.get("foo.bar");
        assertTrue(roles2.contains("foo"));
    }
}