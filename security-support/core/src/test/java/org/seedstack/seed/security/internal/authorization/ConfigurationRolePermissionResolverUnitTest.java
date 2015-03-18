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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import org.seedstack.seed.security.api.Role;

public class ConfigurationRolePermissionResolverUnitTest {

    ConfigurationRolePermissionResolver underTest;

    @Before
    public void before() {
        underTest = new ConfigurationRolePermissionResolver();
    }

    @Test
    public void resolvePermissionsInRole_should_return_permissions() {
        Map<String, Set<String>> rolesMap = Whitebox.getInternalState(underTest, "roles");
        Set<String> permissions = new HashSet<String>();
        permissions.add("bar");
        rolesMap.put("foo", permissions);

        underTest.resolvePermissionsInRole(new Role("foo"));
    }

    @Test
    public void readConfiguration_should_build_roles() {
        Configuration conf = new PropertiesConfiguration();
        conf.addProperty("permissions.foo", "bar, foobar");
        underTest.readConfiguration(conf);
        Map<String, Set<String>> map = Whitebox.getInternalState(underTest, "roles");
        Set<String> permissions = map.get("foo");
        assertTrue(permissions.contains("bar"));
        assertTrue(permissions.contains("foobar"));
    }
}
