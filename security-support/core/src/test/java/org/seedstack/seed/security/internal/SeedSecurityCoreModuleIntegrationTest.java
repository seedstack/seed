/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.shiro.realm.Realm;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.seedstack.seed.security.api.Scope;
import org.seedstack.seed.security.internal.authorization.ConfigurationRoleMapping;
import org.seedstack.seed.security.internal.authorization.ConfigurationRolePermissionResolver;
import org.seedstack.seed.security.internal.configure.RealmConfiguration;
import org.seedstack.seed.security.internal.configure.SeedSecurityConfigurer;
import org.seedstack.seed.security.internal.realms.ConfigurationRealm;
import org.seedstack.seed.security.internal.realms.ShiroRealmAdapter;

public class SeedSecurityCoreModuleIntegrationTest {

	SeedSecurityCoreModule underTest;
	
	SeedSecurityConfigurer securityConfigurer;
	
	@Before
	public void before(){
		securityConfigurer = mock(SeedSecurityConfigurer.class);
		underTest = new SeedSecurityCoreModule(securityConfigurer, new HashMap<String, Class<? extends Scope>>());
	}
	
	@Test
	public void configure_should_bind_wanted_components(){
		Configuration conf = new PropertiesConfiguration();
		conf.addProperty("users.Obiwan", "mdp, jedi");
		when(securityConfigurer.getSecurityConfiguration()).thenReturn(conf);
		
		RealmConfiguration confRealm = new RealmConfiguration("ConfigurationRealm", ConfigurationRealm.class);
		confRealm.setRoleMappingClass(ConfigurationRoleMapping.class);
		confRealm.setRolePermissionResolverClass(ConfigurationRolePermissionResolver.class);
		Collection<RealmConfiguration> confRealms = new ArrayList<RealmConfiguration>();
		confRealms.add(confRealm);
		when(securityConfigurer.getConfigurationRealms()).thenReturn(confRealms);
		
		Injector injector = Guice.createInjector(underTest);
		
		//Verify realm
		Set<Realm> exposedRealms = injector.getInstance(Key.get(new TypeLiteral<Set<Realm>>() {}));
		assertTrue(exposedRealms.size() > 0);
		Realm exposedRealm = exposedRealms.iterator().next();
		assertEquals(ShiroRealmAdapter.class, exposedRealm.getClass());
		
		ConfigurationRealm innerRealm = (ConfigurationRealm) ((ShiroRealmAdapter)exposedRealm).getRealm();
		Set<?> configurationUsers = (Set<?>) Whitebox.getInternalState(innerRealm, "users");
		assertTrue(configurationUsers.size() > 0);
		
		Object roleMapping = Whitebox.getInternalState(innerRealm, "roleMapping");
		assertNotNull(roleMapping);
		assertEquals(ConfigurationRoleMapping.class, roleMapping.getClass());
		
		Object rolePermissionResolver = Whitebox.getInternalState(innerRealm, "rolePermissionResolver");
		assertNotNull(rolePermissionResolver);
		assertEquals(ConfigurationRolePermissionResolver.class, rolePermissionResolver.getClass());		
	}
}
