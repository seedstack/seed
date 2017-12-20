/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.authc.pam.AuthenticationStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.SessionStorageEvaluator;
import org.apache.shiro.mgt.SubjectDAO;
import org.apache.shiro.mgt.SubjectFactory;
import org.apache.shiro.subject.SubjectContext;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;
import org.seedstack.seed.security.internal.SeedSessionStorageEvaluator;

@Config("security")
public class SecurityConfig {
    private SessionConfig sessions = new SessionConfig();
    private CacheConfig cache = new CacheConfig();
    private AuthenticationConfig authentication = new AuthenticationConfig();
    private SubjectConfig subject = new SubjectConfig();
    private List<RealmConfig> realms = new ArrayList<>();
    private Map<String, UserConfig> users = new HashMap<>();
    private Map<String, Set<String>> roles = new HashMap<>();
    private Map<String, Set<String>> permissions = new HashMap<>();

    public SessionConfig sessions() {
        return sessions;
    }

    public CacheConfig cache() {
        return cache;
    }

    public AuthenticationConfig authentication() {
        return authentication;
    }

    public SubjectConfig subject() {
        return subject;
    }

    public List<RealmConfig> getRealms() {
        return Collections.unmodifiableList(realms);
    }

    public SecurityConfig addRealm(RealmConfig realmConfig) {
        realms.add(realmConfig);
        return this;
    }

    public Optional<RealmConfig> getRealm(String name) {
        return realms.stream().filter(realmConfig -> realmConfig.getName().equals(name)).findFirst();
    }

    public Map<String, UserConfig> getUsers() {
        return Collections.unmodifiableMap(users);
    }

    public SecurityConfig addUser(String name, UserConfig userConfig) {
        users.put(name, userConfig);
        return this;
    }

    public Map<String, Set<String>> getRoles() {
        return Collections.unmodifiableMap(roles);
    }

    public SecurityConfig addRole(String name, Set<String> sourceRoles) {
        roles.put(name, sourceRoles);
        return this;
    }

    public Map<String, Set<String>> getPermissions() {
        return Collections.unmodifiableMap(permissions);
    }

    public SecurityConfig addRolePermissions(String role, Set<String> permissions) {
        this.permissions.put(role, permissions);
        return this;
    }

    public static class RealmConfig {
        @SingleValue
        private String name;
        private String roleMapper;
        private String permissionResolver;

        public String getName() {
            return name;
        }

        public RealmConfig setName(String name) {
            this.name = name;
            return this;
        }

        public String getRoleMapper() {
            return roleMapper;
        }

        public RealmConfig setRoleMapper(String roleMapper) {
            this.roleMapper = roleMapper;
            return this;
        }

        public String getPermissionResolver() {
            return permissionResolver;
        }

        public RealmConfig setPermissionResolver(String permissionResolver) {
            this.permissionResolver = permissionResolver;
            return this;
        }
    }

    public static class UserConfig {
        @SingleValue
        private String password = "";
        private Set<String> roles = new HashSet<>();

        public String getPassword() {
            return password;
        }

        public UserConfig setPassword(String password) {
            this.password = password;
            return this;
        }

        public Set<String> getRoles() {
            return Collections.unmodifiableSet(roles);
        }

        public UserConfig addRole(String role) {
            this.roles.add(role);
            return this;
        }
    }

    @Config("sessions")
    public static class SessionConfig {
        @SingleValue
        private boolean enabled = true;
        private long timeout = 1000 * 60 * 15;
        private Class<? extends SessionStorageEvaluator> storageEvaluator = SeedSessionStorageEvaluator.class;

        public boolean isEnabled() {
            return enabled;
        }

        public SessionConfig setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public long getTimeout() {
            return timeout;
        }

        public SessionConfig setTimeout(long timeout) {
            this.timeout = timeout * 1000;
            return this;
        }

        public Class<? extends SessionStorageEvaluator> getStorageEvaluator() {
            return storageEvaluator;
        }

        public SessionConfig setStorageEvaluator(Class<? extends SessionStorageEvaluator> storageEvaluator) {
            this.storageEvaluator = storageEvaluator;
            return this;
        }
    }

    @Config("subject")
    public static class SubjectConfig {
        private Class<? extends SubjectDAO> dao;
        private Class<? extends SubjectContext> context;
        private Class<? extends SubjectFactory> factory;

        public Class<? extends SubjectDAO> getDao() {
            return dao;
        }

        public SubjectConfig setDao(Class<? extends SubjectDAO> dao) {
            this.dao = dao;
            return this;
        }

        public Class<? extends SubjectContext> getContext() {
            return context;
        }

        public SubjectConfig setContext(Class<? extends SubjectContext> context) {
            this.context = context;
            return this;
        }

        public Class<? extends SubjectFactory> getFactory() {
            return factory;
        }

        public SubjectConfig setFactory(Class<? extends SubjectFactory> factory) {
            this.factory = factory;
            return this;
        }
    }

    @Config("authentication")
    public static class AuthenticationConfig {
        @SingleValue
        private Class<? extends AuthenticationStrategy> strategy = AtLeastOneSuccessfulStrategy.class;
        private Class<? extends Authenticator> authenticator = ModularRealmAuthenticator.class;

        public Class<? extends Authenticator> getAuthenticator() {
            return authenticator;
        }

        public AuthenticationConfig setAuthenticator(Class<? extends Authenticator> authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        public Class<? extends AuthenticationStrategy> getStrategy() {
            return strategy;
        }

        public AuthenticationConfig setStrategy(
                Class<? extends AuthenticationStrategy> strategy) {
            this.strategy = strategy;
            return this;
        }

    }

    @Config("cache")
    public static class CacheConfig {
        @SingleValue
        private boolean enabled = true;
        private ItemCacheConfig authentication = new ItemCacheConfig();
        private ItemCacheConfig authorization = new ItemCacheConfig();
        private Class<? extends CacheManager> manager = MemoryConstrainedCacheManager.class;

        public boolean isEnabled() {
            return enabled;
        }

        public CacheConfig setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public ItemCacheConfig authentication() {
            return authentication;
        }

        public ItemCacheConfig authorization() {
            return authorization;
        }

        public Class<? extends CacheManager> getManager() {
            return manager;
        }

        public CacheConfig setManager(Class<? extends CacheManager> manager) {
            this.manager = manager;
            return this;
        }

        public static class ItemCacheConfig {
            @SingleValue
            private boolean enabled = true;
            private String name;

            public boolean isEnabled() {
                return enabled;
            }

            public ItemCacheConfig setEnabled(boolean enabled) {
                this.enabled = enabled;
                return this;
            }

            public String getName() {
                return name;
            }

            public ItemCacheConfig setName(String name) {
                this.name = name;
                return this;
            }
        }
    }
}
