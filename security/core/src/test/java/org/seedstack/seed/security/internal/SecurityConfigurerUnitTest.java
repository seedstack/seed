/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.security.Realm;
import org.seedstack.seed.security.RoleMapping;
import org.seedstack.seed.security.RolePermissionResolver;
import org.seedstack.seed.security.SecurityConfig;
import org.seedstack.seed.security.internal.authorization.EmptyRolePermissionResolver;
import org.seedstack.seed.security.internal.authorization.SameRoleMapping;
import org.seedstack.seed.security.internal.realms.ConfigurationRealm;

public class SecurityConfigurerUnitTest {
    private SecurityConfigurer underTest;
    private SecurityConfig configuration;
    private Map<Class<?>, Collection<Class<?>>> securityClasses;

    @Before
    public void before() {
        configuration = mock(SecurityConfig.class);
        securityClasses = new HashMap<>();
        Collection<Class<?>> classes = new ArrayList<>();
        classes.add(Realm.class);
        securityClasses.put(Realm.class, classes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_should_throw_IllegalArgumentException_if_no_realm_class_provided() {
        securityClasses = new HashMap<>();
        underTest = new SecurityConfigurer(configuration, securityClasses, null);
    }

    @Test
    public void getConfigurationRealms_should_use_default_realm_if_no_realm_defined_in_configuration() {
        underTest = new SecurityConfigurer(configuration, securityClasses, null);
        Collection<RealmConfiguration> realms = underTest.getConfigurationRealms();
        assertEquals(1, realms.size());
        assertEquals(ConfigurationRealm.class, realms.iterator().next().getRealmClass());
        assertEquals("ConfigurationRealm", realms.iterator().next().getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void
    getConfigurationRealms_should_throw_IllegalArgumentException_if_no_realm_class_correspond_to_configuration() {
        when(configuration.getRealms()).thenReturn(ImmutableList.of(new SecurityConfig.RealmConfig().setName("toto")));
        underTest = new SecurityConfigurer(configuration, securityClasses, null);

        underTest.getConfigurationRealms();
    }

    @Test
    public void getConfigurationRealms_should_return_a_configurationRealm() {
        when(configuration.getRealms()).thenReturn(ImmutableList.of(new SecurityConfig.RealmConfig().setName("Realm")));
        underTest = new SecurityConfigurer(configuration, securityClasses, null);

        underTest.getConfigurationRealms().iterator().next();
        RealmConfiguration realm = underTest.getConfigurationRealms().iterator().next();
        assertEquals(Realm.class, realm.getRealmClass());
        assertEquals("Realm", realm.getName());
        assertEquals(SameRoleMapping.class, realm.getRoleMappingClass());
        assertEquals(EmptyRolePermissionResolver.class, realm.getRolePermissionResolverClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getConfigurationRealms_should_throw_IllegalArgumentException_if_no_class_for_component_given() {
        when(configuration.getRealms()).thenReturn(
                ImmutableList.of(new SecurityConfig.RealmConfig().setName("Realm").setRoleMapper("RoleMapping")));
        underTest = new SecurityConfigurer(configuration, securityClasses, null);

        RealmConfiguration realm = underTest.getConfigurationRealms().iterator().next();
        assertEquals(RoleMapping.class, realm.getRoleMappingClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getConfigurationRealms_should_throw_IllegalArgumentException_if_no_class_for_component_match_property
            () {
        Collection<Class<?>> roleMappingClasses = new ArrayList<>();
        roleMappingClasses.add(RoleMapping.class);
        securityClasses.put(RoleMapping.class, roleMappingClasses);
        when(configuration.getRealms()).thenReturn(
                ImmutableList.of(new SecurityConfig.RealmConfig().setName("Realm").setRoleMapper("toto")));
        underTest = new SecurityConfigurer(configuration, securityClasses, null);

        RealmConfiguration realm = underTest.getConfigurationRealms().iterator().next();
        assertEquals(RoleMapping.class, realm.getRoleMappingClass());
    }

    @Test
    public void getConfigurationRealms_should_return_a_configurationRealm_with_one_roleMaping() {
        Collection<Class<?>> roleMappingClasses = new ArrayList<>();
        roleMappingClasses.add(RoleMapping.class);
        securityClasses.put(RoleMapping.class, roleMappingClasses);
        when(configuration.getRealms()).thenReturn(
                ImmutableList.of(new SecurityConfig.RealmConfig().setName("Realm").setRoleMapper("RoleMapping")));
        underTest = new SecurityConfigurer(configuration, securityClasses, null);

        RealmConfiguration realm = underTest.getConfigurationRealms().iterator().next();
        assertEquals(RoleMapping.class, realm.getRoleMappingClass());
    }

    @Test
    public void getConfigurationRealms_should_return_a_configurationRealm_with_one_rolePermissionResolver() {
        Collection<Class<?>> rolePermissionResolverClasses = new ArrayList<>();
        rolePermissionResolverClasses.add(RolePermissionResolver.class);
        securityClasses.put(RolePermissionResolver.class, rolePermissionResolverClasses);
        when(configuration.getRealms()).thenReturn(ImmutableList.of(
                new SecurityConfig.RealmConfig().setName("Realm").setPermissionResolver("RolePermissionResolver")));
        underTest = new SecurityConfigurer(configuration, securityClasses, null);

        RealmConfiguration realm = underTest.getConfigurationRealms().iterator().next();
        assertEquals(RolePermissionResolver.class, realm.getRolePermissionResolverClass());
    }
}
