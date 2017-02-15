/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
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
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.realm.Realm;
import org.seedstack.seed.security.PrincipalCustomizer;
import org.seedstack.seed.security.RoleMapping;
import org.seedstack.seed.security.RolePermissionResolver;
import org.seedstack.seed.security.Scope;
import org.seedstack.seed.security.SecuritySupport;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        bind(Configuration.class).annotatedWith(Names.named("seed-security-config")).toInstance(securityConfigurer.getSecurityConfiguration());

        bind(ShiroRealmAdapter.class);

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
        expose(Configuration.class).annotatedWith(Names.named("seed-security-config"));
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

        bind(new TypeLiteral<Set<Class<? extends org.seedstack.seed.security.Realm>>>() {}).toInstance(apiRealmClasses);
        bind(new TypeLiteral<Set<Realm>>() {}).toProvider(new RealmProvider(securityConfigurer.getSecurityConfiguration())).asEagerSingleton();
    }

    static class RealmProvider implements Provider<Set<Realm>> {
        private final Configuration securityConfiguration;
        @Inject
        private Injector injector;
        @Inject
        private Set<Class<? extends org.seedstack.seed.security.Realm>> realmClasses;
        private Set<Realm> realms;

        RealmProvider(Configuration securityConfiguration) {
            this.securityConfiguration = securityConfiguration;
        }

        @Override
        public Set<Realm> get() {
            if (realms == null) {
                realms = new HashSet<Realm>();
                for (Class<? extends org.seedstack.seed.security.Realm> seedRealmClass : realmClasses) {
                    ShiroRealmAdapter realmAdapter = injector.getInstance(ShiroRealmAdapter.class);
                    realmAdapter.setRealm(injector.getInstance(seedRealmClass));

                    // Authentication cache
                    realmAdapter.setAuthenticationCachingEnabled(securityConfiguration.getBoolean("cache.authentication.enabled", true));
                    String authenticationCacheName = securityConfiguration.getString("cache.authentication.name");
                    if (!Strings.isNullOrEmpty(authenticationCacheName)) {
                        realmAdapter.setAuthenticationCacheName(authenticationCacheName);
                    }

                    // Authorization cache
                    realmAdapter.setAuthorizationCachingEnabled(securityConfiguration.getBoolean("cache.authorization.enabled", true));
                    String authorizationCacheName = securityConfiguration.getString("cache.authorization.name");
                    if (!Strings.isNullOrEmpty(authorizationCacheName)) {
                        realmAdapter.setAuthorizationCacheName(authorizationCacheName);
                    }

                    realms.add(realmAdapter);
                }
            }
            return realms;
        }
    }
}
