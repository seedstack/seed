/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import com.google.inject.Injector;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.realm.Realm;
import org.seedstack.seed.security.PrincipalCustomizer;
import org.seedstack.seed.security.RoleMapping;
import org.seedstack.seed.security.RolePermissionResolver;
import org.seedstack.seed.security.Scope;
import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.security.internal.configure.RealmConfiguration;
import org.seedstack.seed.security.internal.configure.SeedSecurityConfigurer;
import org.seedstack.seed.security.internal.realms.ShiroRealmAdapter;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class SecurityInternalModule extends PrivateModule {
    private final Map<String, Class<? extends Scope>> scopeClasses;
    private final SeedSecurityConfigurer securityConfigurer;

    SecurityInternalModule(SeedSecurityConfigurer securityConfigurer, Map<String, Class<? extends Scope>> scopeClasses) {
        this.securityConfigurer = securityConfigurer;
        this.scopeClasses = scopeClasses;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void configure() {
        bind(Configuration.class).annotatedWith(Names.named("seed-security-config")).toInstance(securityConfigurer.getSecurityConfiguration());
        bind(CacheManager.class).to(MemoryConstrainedCacheManager.class);
        try {
            bind(ShiroRealmAdapter.class).toConstructor(ShiroRealmAdapter.class.getConstructor(CacheManager.class));
        } catch (NoSuchMethodException e) {
            //if the constructor does not exist, we have a problem with our realm
            throw new RuntimeException(e);
        }
        bind(new TypeLiteral<Map<String, Class<? extends Scope>>>() {}).toInstance(scopeClasses);
        bind(SecuritySupport.class).to(ShiroSecuritySupport.class);
        bindRealms();
        Multibinder<PrincipalCustomizer> principalCustomizers = Multibinder.newSetBinder(binder(), PrincipalCustomizer.class);
        for (Class<? extends PrincipalCustomizer> customizerClass : securityConfigurer.getPrincipalCustomizers()) {
            principalCustomizers.addBinding().to(customizerClass);
        }

        expose(new TypeLiteral<Set<Realm>>() {});
        expose(new TypeLiteral<Set<PrincipalCustomizer>>() {});
        expose(SecuritySupport.class);
    }

    private void bindRealms() {
        Collection<RealmConfiguration> realms = securityConfigurer.getConfigurationRealms();
        Set<Class<? extends org.seedstack.seed.security.Realm>> apiRealmClasses = new HashSet<Class<? extends org.seedstack.seed.security.Realm>>();
        for (RealmConfiguration realm : realms) {
            bind(realm.getRealmClass());
            apiRealmClasses.add(realm.getRealmClass());
            bind(RolePermissionResolver.class).annotatedWith(Names.named(realm.getName() + "-role-permission-resolver")).to(realm.getRolePermissionResolverClass());
            bind(RoleMapping.class).annotatedWith(Names.named(realm.getName() + "-role-mapping")).to(realm.getRoleMappingClass());
        }
        bind(new TypeLiteral<Set<Class<? extends org.seedstack.seed.security.Realm>>>() {
        }).toInstance(apiRealmClasses);
        bind(new TypeLiteral<Set<Realm>>() {
        }).toProvider(RealmProvider.class).asEagerSingleton();
    }

    static class RealmProvider implements Provider<Set<Realm>> {

        @Inject
        private Injector injector;

        @Inject
        private Set<Class<? extends org.seedstack.seed.security.Realm>> realmClasses;

        private Set<Realm> realms;

        @Override
        public Set<Realm> get() {
            if (realms == null) {
                realms = new HashSet<Realm>();
                for (Class<? extends org.seedstack.seed.security.Realm> seedRealmClass : realmClasses) {
                    org.seedstack.seed.security.Realm realmInstance = injector.getInstance(seedRealmClass);
                    ShiroRealmAdapter realmAdapter = injector.getInstance(ShiroRealmAdapter.class);
                    realmAdapter.setRealm(realmInstance);
                    realms.add(realmAdapter);
                }
            }
            return realms;
        }
    }
}
