/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.apache.shiro.util.CollectionUtils;
import org.seedstack.seed.security.PrincipalCustomizer;
import org.seedstack.seed.security.Realm;
import org.seedstack.seed.security.RoleMapping;
import org.seedstack.seed.security.RolePermissionResolver;
import org.seedstack.seed.security.SecurityConfig;
import org.seedstack.seed.security.internal.authorization.ConfigurationRoleMapping;
import org.seedstack.seed.security.internal.authorization.ConfigurationRolePermissionResolver;
import org.seedstack.seed.security.internal.authorization.EmptyRolePermissionResolver;
import org.seedstack.seed.security.internal.authorization.SameRoleMapping;
import org.seedstack.seed.security.internal.realms.ConfigurationRealm;

class SecurityConfigurer {
    private static final String REALMS_KEY = "realms";
    private static final String ROLE_MAPPING_KEY = ".role-mapping";
    private static final String ROLE_PERMISSION_RESOLVER_KEY = ".role-permission-resolver";

    private static final Class<? extends Realm> DEFAULT_REALM = ConfigurationRealm.class;

    private static final Class<? extends RoleMapping> DEFAULT_ROLE_MAPPING = SameRoleMapping.class;
    private static final Class<? extends RoleMapping> CONFIGURATION_ROLE_MAPPING = ConfigurationRoleMapping.class;

    private static final Class<? extends RolePermissionResolver> DEFAULT_ROLE_PERMISSION_RESOLVER =
            EmptyRolePermissionResolver.class;
    private static final Class<? extends RolePermissionResolver> CONFIGURATION_ROLE_PERMISSION_RESOLVER =
            ConfigurationRolePermissionResolver.class;

    private final SecurityConfig securityConfig;
    private final Map<Class<?>, Collection<Class<?>>> securityClasses;
    private final Collection<Class<? extends PrincipalCustomizer<?>>> principalCustomizerClasses;
    private final Collection<RealmConfiguration> configurationRealms = new ArrayList<>();

    SecurityConfigurer(SecurityConfig securityConfig, Map<Class<?>, Collection<Class<?>>> securityClasses,
            Collection<Class<? extends PrincipalCustomizer<?>>> principalCustomizerClasses) {
        this.securityConfig = securityConfig;
        this.securityClasses = securityClasses;
        this.principalCustomizerClasses = principalCustomizerClasses;
        if (CollectionUtils.isEmpty(securityClasses.get(Realm.class))) {
            throw new IllegalArgumentException("No realm class provided !");
        }
        buildRealms();
    }

    Collection<Class<? extends PrincipalCustomizer<?>>> getPrincipalCustomizers() {
        if (principalCustomizerClasses != null) {
            return principalCustomizerClasses;
        }
        return Collections.emptyList();
    }

    Collection<RealmConfiguration> getConfigurationRealms() {
        return Collections.unmodifiableCollection(configurationRealms);
    }

    SecurityConfig getSecurityConfiguration() {
        return this.securityConfig;
    }

    @SuppressWarnings("unchecked")
    private void buildRealms() {
        if (securityConfig.getRealms().isEmpty()) {
            RealmConfiguration confRealm = new RealmConfiguration(DEFAULT_REALM.getSimpleName(), DEFAULT_REALM);
            confRealm.setRolePermissionResolverClass(findRolePermissionResolver(null, confRealm));
            confRealm.setRoleMappingClass(findRoleMapping(null, confRealm));
            configurationRealms.add(confRealm);
        } else {
            for (SecurityConfig.RealmConfig realmConfig : securityConfig.getRealms()) {
                Class<? extends Realm> realmClass = (Class<? extends Realm>) findClass(realmConfig.getName(),
                        securityClasses.get(Realm.class));
                if (realmClass == null) {
                    throw new IllegalArgumentException(
                            "Unknown realm defined in property " + REALMS_KEY + " : " + realmConfig.getName());
                }
                RealmConfiguration confRealm = new RealmConfiguration(realmConfig.getName(), realmClass);
                confRealm.setRolePermissionResolverClass(findRolePermissionResolver(realmConfig, confRealm));
                confRealm.setRoleMappingClass(findRoleMapping(realmConfig, confRealm));
                configurationRealms.add(confRealm);
            }
        }
    }

    private Class<? extends RolePermissionResolver> findRolePermissionResolver(SecurityConfig.RealmConfig realmConfig,
            RealmConfiguration realm) {
        if (realmConfig == null || realmConfig.getPermissionResolver() == null) {
            if (securityConfig.getPermissions().isEmpty()) {
                return DEFAULT_ROLE_PERMISSION_RESOLVER;
            }
            return CONFIGURATION_ROLE_PERMISSION_RESOLVER;
        }
        return findRealmComponent(realm.getName(), realmConfig.getPermissionResolver(), RolePermissionResolver.class);
    }

    private Class<? extends RoleMapping> findRoleMapping(SecurityConfig.RealmConfig realmConfig,
            RealmConfiguration realm) {
        if (realmConfig == null || realmConfig.getRoleMapper() == null) {
            if (securityConfig.getRoles().isEmpty()) {
                return DEFAULT_ROLE_MAPPING;
            }
            return CONFIGURATION_ROLE_MAPPING;
        }
        return findRealmComponent(realm.getName(), realmConfig.getRoleMapper(), RoleMapping.class);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<? extends T> findRealmComponent(String realmName, String componentName,
            Class<? extends T> clazz) {
        Class<? extends T> componentClass;
        if (CollectionUtils.isEmpty(securityClasses.get(clazz))) {
            throw new IllegalArgumentException("No class of type " + componentName + " were found");
        }
        componentClass = (Class<? extends T>) findClass(componentName, securityClasses.get(clazz));
        if (componentClass == null) {
            throw new IllegalArgumentException("Unknown property value " + componentName + " for realm " + realmName);
        }
        return componentClass;
    }

    private Class<?> findClass(String name, Collection<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (clazz.getSimpleName().equals(name)) {
                return clazz;
            }
        }
        return null;
    }
}
