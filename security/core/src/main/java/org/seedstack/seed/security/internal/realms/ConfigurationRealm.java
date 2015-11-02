/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.realms;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.security.*;
import org.seedstack.seed.security.principals.PrincipalProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A realm that authentifies users and gives authorities with a configuration file.
 * 
 * @author yves.dautremay@mpsa.com
 */
public class ConfigurationRealm implements Realm {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationRealm.class);

    /** props section suffix */
    public static final String USER_SECTION_NAME = "users";

    private final Set<ConfigurationUser> users = new HashSet<ConfigurationUser>();

    private RoleMapping roleMapping;

    private RolePermissionResolver rolePermissionResolver;

    @Override
    public Set<String> getRealmRoles(PrincipalProvider<?> identityPrincipal, Collection<PrincipalProvider<?>> otherPrincipals) {
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

    /**
     * Reads the configuration props to initialize authorized users and their rights.
     * 
     * @param securityConfiguration the configuration concerning security
     */
    @Inject
    public void readConfiguration(@Named("seed-security-config") Configuration securityConfiguration) {
        Configuration usersConfig = securityConfiguration.subset(USER_SECTION_NAME);
        if (usersConfig.isEmpty()) {
            LOGGER.warn("{} defined, but the configuration defines no user", getClass().getSimpleName());
            return;
        }
        users.clear();
        processUsersConfiguration(usersConfig);
    }

    private void processUsersConfiguration(Configuration usersConfiguration) {
        Iterator<String> keys = usersConfiguration.getKeys();
        while (keys.hasNext()) {
            String userName = keys.next();
            ConfigurationUser user = new ConfigurationUser(userName);
            String[] values = usersConfiguration.getStringArray(userName);
            if (values.length == 0) {
                user.password = "";
            } else {
                user.password = values[0];
                for (int i = 1; i < values.length; i++) {
                    user.roles.add(values[i]);
                }
            }
            users.add(user);
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

    /**
     * Setter roleMapping
     * 
     * @param roleMapping the role mapping
     */
    @Inject
    public void setRoleMapping(@Named("ConfigurationRealm-role-mapping") RoleMapping roleMapping) {
        this.roleMapping = roleMapping;
    }

    /**
     * Setter rolePermissionResolver
     * 
     * @param rolePermissionResolver the rolePermissionResolver
     */
    @Inject
    public void setRolePermissionResolver(@Named("ConfigurationRealm-role-permission-resolver") RolePermissionResolver rolePermissionResolver) {
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
     * Class to represent a user from the configuration. In the file, key is the name, first value is the password, following values are the roles.
     * 
     * @author yves.dautremay@mpsa.com
     */
    static class ConfigurationUser {

        private final String username;

        private final Set<String> roles = new HashSet<String>();

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
