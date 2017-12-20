/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal.realms;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.seedstack.seed.security.AuthenticationException;
import org.seedstack.seed.security.AuthenticationInfo;
import org.seedstack.seed.security.AuthenticationToken;
import org.seedstack.seed.security.IncorrectCredentialsException;
import org.seedstack.seed.security.Realm;
import org.seedstack.seed.security.RoleMapping;
import org.seedstack.seed.security.RolePermissionResolver;
import org.seedstack.seed.security.SecurityConfig;
import org.seedstack.seed.security.UnknownAccountException;
import org.seedstack.seed.security.UnsupportedTokenException;
import org.seedstack.seed.security.UsernamePasswordToken;
import org.seedstack.seed.security.principals.PrincipalProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A realm that authentifies users and gives authorities with a configuration file.
 */
public class ConfigurationRealm implements Realm {

    /**
     * props section suffix
     */
    public static final String USER_SECTION_NAME = "users";
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationRealm.class);
    private final Set<ConfigurationUser> users = new HashSet<>();

    private RoleMapping roleMapping;

    private RolePermissionResolver rolePermissionResolver;

    @Override
    public Set<String> getRealmRoles(PrincipalProvider<?> identityPrincipal,
            Collection<PrincipalProvider<?>> otherPrincipals) {
        ConfigurationUser user = findUser(identityPrincipal.getPrincipal().toString());
        if (user != null) {
            return user.roles;
        }
        return Collections.emptySet();
    }

    @Override
    public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (!(token instanceof UsernamePasswordToken)) {
            throw new UnsupportedTokenException("ConfigurationRealm only supports UsernamePasswordToken");
        }
        UsernamePasswordToken userNamePasswordToken = (UsernamePasswordToken) token;
        ConfigurationUser user = findUser(userNamePasswordToken.getUsername());
        if (user == null) {
            throw new UnknownAccountException("Unknown user " + userNamePasswordToken.getUsername());
        }
        if (!user.password.equals(new String(userNamePasswordToken.getPassword()))) {
            throw new IncorrectCredentialsException();
        }
        return new AuthenticationInfo(userNamePasswordToken.getUsername(), userNamePasswordToken.getPassword());
    }

    @Inject
    void readConfiguration(SecurityConfig securityConfig) {
        if (securityConfig.getUsers().isEmpty()) {
            LOGGER.warn("{} defined, but the configuration defines no user", getClass().getSimpleName());
        } else {
            users.clear();
            for (Map.Entry<String, SecurityConfig.UserConfig> entry : securityConfig.getUsers().entrySet()) {
                ConfigurationUser user = new ConfigurationUser(entry.getKey());
                SecurityConfig.UserConfig userConfig = entry.getValue();
                user.password = userConfig.getPassword();
                user.roles.addAll(userConfig.getRoles());
                users.add(user);
            }
        }
    }

    @Override
    public RoleMapping getRoleMapping() {
        return this.roleMapping;
    }

    /**
     * Setter roleMapping
     *
     * @param roleMapping the role mapping
     */
    @Inject
    public void setRoleMapping(@Named("ConfigurationRealm-role-mapping") RoleMapping roleMapping) {
        this.roleMapping = roleMapping;
    }

    @Override
    public RolePermissionResolver getRolePermissionResolver() {
        return this.rolePermissionResolver;
    }

    /**
     * Setter rolePermissionResolver
     *
     * @param rolePermissionResolver the rolePermissionResolver
     */
    @Inject
    public void setRolePermissionResolver(
            @Named("ConfigurationRealm-role-permission-resolver") RolePermissionResolver rolePermissionResolver) {
        this.rolePermissionResolver = rolePermissionResolver;
    }

    private ConfigurationUser findUser(String username) {
        for (ConfigurationUser user : users) {
            if (user.username.equals(username)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public Class<? extends AuthenticationToken> supportedToken() {
        return UsernamePasswordToken.class;
    }

    /**
     * Class to represent a user from the configuration. In the file, key is the name, first value is the password,
     * following values are the roles.
     */
    static class ConfigurationUser {

        private final String username;

        private final Set<String> roles = new HashSet<>();

        private String password;

        ConfigurationUser(String username) {
            this.username = username;
        }

        public Set<String> getRoles() {
            return roles;
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
