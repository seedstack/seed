/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import org.apache.shiro.realm.Realm;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.seedstack.seed.security.SecurityConfig;
import org.seedstack.seed.security.internal.authorization.ConfigurationRoleMapping;
import org.seedstack.seed.security.internal.authorization.ConfigurationRolePermissionResolver;
import org.seedstack.seed.security.internal.realms.ConfigurationRealm;

public class SecurityInternalModuleIntegrationTest {
    private SecurityInternalModule underTest;
    private SecurityConfigurer securityConfigurer;
    private SecurityGuiceConfigurer securityGuiceConfigurer;

    @Before
    public void before() {
        securityConfigurer = mock(SecurityConfigurer.class);
        securityGuiceConfigurer = mock(SecurityGuiceConfigurer.class);
        underTest = new SecurityInternalModule(securityConfigurer, new HashMap<>());
    }

    @Test
    public void configure_should_bind_wanted_components() {
        SecurityConfig conf = new SecurityConfig();
        conf.addUser("Obiwan", new SecurityConfig.UserConfig().setPassword("mdp").addRole("jedi"));
        when(securityConfigurer.getSecurityConfiguration()).thenReturn(conf);

        RealmConfiguration confRealm = new RealmConfiguration("ConfigurationRealm", ConfigurationRealm.class);
        confRealm.setRoleMappingClass(ConfigurationRoleMapping.class);
        confRealm.setRolePermissionResolverClass(ConfigurationRolePermissionResolver.class);
        Collection<RealmConfiguration> confRealms = new ArrayList<>();
        confRealms.add(confRealm);
        when(securityConfigurer.getConfigurationRealms()).thenReturn(confRealms);

        Injector injector = Guice.createInjector(underTest, new DefaultSecurityModule(securityGuiceConfigurer));

        //Verify realm
        Set<Realm> exposedRealms = injector.getInstance(Key.get(new TypeLiteral<Set<Realm>>() {
        }));
        assertTrue(exposedRealms.size() > 0);
        Realm exposedRealm = exposedRealms.iterator().next();
        assertEquals(ShiroRealmAdapter.class, exposedRealm.getClass());

        ConfigurationRealm innerRealm = (ConfigurationRealm) ((ShiroRealmAdapter) exposedRealm).getRealm();
        Set<?> users = Whitebox.getInternalState(innerRealm, "users");
        assertTrue(users.size() > 0);

        assertNotNull(innerRealm.getRoleMapping());
        assertEquals(ConfigurationRoleMapping.class, innerRealm.getRoleMapping().getClass());

        assertNotNull(innerRealm.getRolePermissionResolver());
        assertEquals(ConfigurationRolePermissionResolver.class, innerRealm.getRolePermissionResolver().getClass());
    }
}
