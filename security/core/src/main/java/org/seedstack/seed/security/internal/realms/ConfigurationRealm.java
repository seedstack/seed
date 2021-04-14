/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.realms;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.seedstack.seed.security.*;
import org.seedstack.seed.security.internal.SeedUsernamePasswordToken;
import org.seedstack.seed.security.principals.PrincipalProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * A realm that authenticate users and gives authorities using SeedStack configuration.
 */
public class ConfigurationRealm implements Realm {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationRealm.class);
    private final Set<ConfigurationUser> users = new HashSet<>();
    private final RoleMapping roleMapping;
    private final RolePermissionResolver rolePermissionResolver;

    @Inject
    protected ConfigurationRealm(@Named("ConfigurationRealm-role-mapping") RoleMapping roleMapping,
                                 @Named("ConfigurationRealm-role-permission-resolver") RolePermissionResolver rolePermissionResolver,
                                 SecurityConfig securityConfig) {
        this.roleMapping = roleMapping;
        this.rolePermissionResolver = rolePermissionResolver;
        if (securityConfig.getUsers().isEmpty()) {
            LOGGER.warn("{} is enabled, but no user is defined in configuration", getClass().getSimpleName());
        } else {
            for (Map.Entry<String, SecurityConfig.UserConfig> entry : securityConfig.getUsers().entrySet()) {
                SecurityConfig.UserConfig userConfig = entry.getValue();
                users.add(new ConfigurationUser(entry.getKey(), userConfig.getPassword(), userConfig.getRoles()));
            }
        }
    }

    @Override
    public Set<String> getRealmRoles(PrincipalProvider<?> identityPrincipal,
                                     Collection<PrincipalProvider<?>> otherPrincipals) {
        ConfigurationUser user = findUser(identityPrincipal.get().toString());
        if (user != null) {
            return user.roles;
        }
        return Collections.emptySet();
    }

    @Override
    public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (token instanceof SeedUsernamePasswordToken) {
            UsernamePasswordToken userNamePasswordToken = (SeedUsernamePasswordToken) token;
            ConfigurationUser user = findUser(userNamePasswordToken.getUsername());
            if (user == null) {
                throw new UnknownAccountException("Unknown user " + userNamePasswordToken.getUsername());
            }
            if (!user.password.equals(new String(userNamePasswordToken.getPassword()))) {
                throw new IncorrectCredentialsException();
            }
            return new AuthenticationInfo(userNamePasswordToken.getUsername(), userNamePasswordToken.getPassword());
        } else {
            throw new UnsupportedTokenException("ConfigurationRealm only supports UsernamePasswordToken");
        }
    }

    @Override
    public RoleMapping getRoleMapping() {
        return this.roleMapping;
    }

    @Override
    public RolePermissionResolver getRolePermissionResolver() {
        return this.rolePermissionResolver;
    }

    @Override
    public Class<? extends AuthenticationToken> supportedToken() {
        return SeedUsernamePasswordToken.class;
    }

    private ConfigurationUser findUser(String username) {
        for (ConfigurationUser user : users) {
            if (user.username.equals(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Class to represent a user from the configuration. In the file, key is the name, first value is the password,
     * following values are the roles.
     */
    private static class ConfigurationUser {
        private final String username;
        private final String password;
        private final Set<String> roles;

        ConfigurationUser(String username, String password, Set<String> roles) {
            this.username = username;
            this.password = password;
            this.roles = new HashSet<>(roles);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ConfigurationUser configurationUser = (ConfigurationUser) o;
            return username.equals(configurationUser.username);
        }

        @Override
        public int hashCode() {
            return username.hashCode();
        }
    }
}
