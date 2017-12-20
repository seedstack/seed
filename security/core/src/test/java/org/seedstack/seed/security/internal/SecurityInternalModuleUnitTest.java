/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.Injector;
import com.google.inject.PrivateBinder;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedElementBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.shiro.realm.Realm;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.seedstack.seed.security.SecurityConfig;
import org.seedstack.seed.security.internal.realms.ConfigurationRealm;

public class SecurityInternalModuleUnitTest {

    private SecurityInternalModule underTest;

    private PrivateBinder binder;

    private SecurityConfigurer securityConfigurer;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Before
    public void before() {
        binder = mock(PrivateBinder.class);
        AnnotatedBindingBuilder ab = mock(AnnotatedBindingBuilder.class);
        when(binder.bind(any(Class.class))).thenReturn(ab);
        when(ab.annotatedWith(any(Annotation.class))).thenReturn(ab);
        AnnotatedElementBuilder aeb = mock(AnnotatedElementBuilder.class);
        when(binder.expose(any(Class.class))).thenReturn(aeb);
        ScopedBindingBuilder sb = mock(ScopedBindingBuilder.class);
        when(ab.toProvider(any(Provider.class))).thenReturn(sb);
        when(binder.bind(any(TypeLiteral.class))).thenReturn(ab);
        when(binder.skipSources(any(Class.class), any(Class.class))).thenReturn(binder);
        securityConfigurer = mock(SecurityConfigurer.class);
        underTest = new SecurityInternalModule(securityConfigurer, new HashMap<>());
        Whitebox.setInternalState(underTest, "binder", binder);
    }

    @Test
    public void testConfigure() {
        Set<RealmConfiguration> realmConfs = new HashSet<>();
        realmConfs.add(new RealmConfiguration("ConfigurationRealm", ConfigurationRealm.class));
        when(securityConfigurer.getConfigurationRealms()).thenReturn(realmConfs);
        when(securityConfigurer.getSecurityConfiguration()).thenReturn(new SecurityConfig());

        underTest.configure();
    }

    @Test
    public void testProvider() {
        SecurityInternalModule.RealmProvider rp = new SecurityInternalModule.RealmProvider(new SecurityConfig());
        Injector i = mock(Injector.class);
        ShiroRealmAdapter adapter = new ShiroRealmAdapter();
        when(i.getInstance(ShiroRealmAdapter.class)).thenReturn(adapter);
        Set<Class<? extends org.seedstack.seed.security.Realm>> realmClasses = new HashSet<>();
        realmClasses.add(ConfigurationRealm.class);

        Whitebox.setInternalState(rp, "injector", i);
        Whitebox.setInternalState(rp, "realmClasses", realmClasses);

        Set<Realm> realms = rp.get();
        assertEquals(1, realms.size());
        assertEquals(adapter, realms.iterator().next());
    }
}
