/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal;

import com.google.common.base.Strings;
import com.google.inject.Injector;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.apache.shiro.realm.Realm;
import org.seedstack.seed.security.PrincipalCustomizer;
import org.seedstack.seed.security.RoleMapping;
import org.seedstack.seed.security.RolePermissionResolver;
import org.seedstack.seed.security.Scope;
import org.seedstack.seed.security.SecurityConfig;
import org.seedstack.seed.security.SecuritySupport;

class SecurityInternalModule extends PrivateModule {
    private final Map<String, Class<? extends Scope>> scopeClasses;
    private final SecurityConfigurer securityConfigurer;

    SecurityInternalModule(SecurityConfigurer securityConfigurer, Map<String, Class<? extends Scope>> scopeClasses) {
        this.securityConfigurer = securityConfigurer;
        this.scopeClasses = scopeClasses;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void configure() {
        bind(ShiroRealmAdapter.class);
        bind(new ScopeClassesTypeLiteral()).toInstance(scopeClasses);
        bindRealms();
        bindPrincipalCustomizers();
        bind(SecurityConfig.class).toInstance(securityConfigurer.getSecurityConfiguration());
        expose(SecurityConfig.class);
        bind(SecuritySupport.class).to(ShiroSecuritySupport.class);
        expose(SecuritySupport.class);
    }

    private void bindPrincipalCustomizers() {
        Multibinder<PrincipalCustomizer> principalCustomizers = Multibinder.newSetBinder(binder(),
                PrincipalCustomizer.class);
        for (Class<? extends PrincipalCustomizer> customizerClass : securityConfigurer.getPrincipalCustomizers()) {
            principalCustomizers.addBinding().to(customizerClass);
        }
        expose(new PrincipalCustomizersTypeLiteral());
    }

    private void bindRealms() {
        Collection<RealmConfiguration> realms = securityConfigurer.getConfigurationRealms();
        Set<Class<? extends org.seedstack.seed.security.Realm>> apiRealmClasses = new HashSet<>();

        for (RealmConfiguration realm : realms) {
            bind(realm.getRealmClass());
            apiRealmClasses.add(realm.getRealmClass());
            bind(RolePermissionResolver.class).annotatedWith(
                    Names.named(realm.getName() + "-role-permission-resolver")).to(
                    realm.getRolePermissionResolverClass());
            bind(RoleMapping.class).annotatedWith(Names.named(realm.getName() + "-role-mapping")).to(
                    realm.getRoleMappingClass());
        }

        bind(new RealmClassesTypeLiteral()).toInstance(apiRealmClasses);
        bind(new RealmsTypeLiteral()).toProvider(
                new RealmProvider(securityConfigurer.getSecurityConfiguration())).asEagerSingleton();
        expose(new RealmsTypeLiteral());
    }

    static class RealmProvider implements Provider<Set<Realm>> {
        private final SecurityConfig securityConfiguration;
        @Inject
        private Injector injector;
        @Inject
        private Set<Class<? extends org.seedstack.seed.security.Realm>> realmClasses;
        private Set<Realm> realms;

        RealmProvider(SecurityConfig securityConfiguration) {
            this.securityConfiguration = securityConfiguration;
        }

        @Override
        public Set<Realm> get() {
            if (realms == null) {
                realms = new HashSet<>();
                for (Class<? extends org.seedstack.seed.security.Realm> seedRealmClass : realmClasses) {
                    ShiroRealmAdapter realmAdapter = injector.getInstance(ShiroRealmAdapter.class);
                    realmAdapter.setRealm(injector.getInstance(seedRealmClass));

                    if (securityConfiguration.cache().isEnabled()) {
                        realmAdapter.setCachingEnabled(true);

                        // Authentication cache
                        realmAdapter.setAuthenticationCachingEnabled(
                                securityConfiguration.cache().authentication().isEnabled());
                        String authenticationCacheName = securityConfiguration.cache().authentication().getName();
                        if (!Strings.isNullOrEmpty(authenticationCacheName)) {
                            realmAdapter.setAuthenticationCacheName(authenticationCacheName);
                        }

                        // Authorization cache
                        realmAdapter.setAuthorizationCachingEnabled(
                                securityConfiguration.cache().authorization().isEnabled());
                        String authorizationCacheName = securityConfiguration.cache().authorization().getName();
                        if (!Strings.isNullOrEmpty(authorizationCacheName)) {
                            realmAdapter.setAuthorizationCacheName(authorizationCacheName);
                        }
                    } else {
                        realmAdapter.setCachingEnabled(false);
                    }

                    realms.add(realmAdapter);
                }
            }
            return realms;
        }
    }

    private static class ScopeClassesTypeLiteral extends TypeLiteral<Map<String, Class<? extends Scope>>> {
    }

    private static class PrincipalCustomizersTypeLiteral extends TypeLiteral<Set<PrincipalCustomizer>> {
    }

    private static class RealmClassesTypeLiteral extends TypeLiteral<Set<Class<? extends org.seedstack.seed.security
            .Realm>>> {
    }

    private static class RealmsTypeLiteral extends TypeLiteral<Set<Realm>> {
    }
}
