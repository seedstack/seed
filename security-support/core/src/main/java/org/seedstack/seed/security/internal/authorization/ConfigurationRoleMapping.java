/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.authorization;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.security.api.Role;
import org.seedstack.seed.security.api.RoleMapping;
import org.seedstack.seed.security.api.Scope;
import org.seedstack.seed.security.api.principals.PrincipalProvider;
import org.seedstack.seed.security.spi.SecurityErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Resolve the role mappings from a Configuration:
 *
 * <pre>
 *     admin = ADMIN, DEV
 * </pre>
 *
 * Means that the admin local role will be given to any identified subject to which the realm(s) have given the ADMIN
 * <strong>or</strong> DEV role.
 * <p>
 * Roles can be given automatically to any user (whatever their real authorizations given by the realms) by using
 * the '*' wildcard:
 *
 * <pre>
 *     reader = *
 * </pre>
 *
 * Means that every identified subject will have the reader role.
 * <p>
 * This implementation handles simple scopes:
 *
 * <pre>
 *     admin = ADMIN.{SCOPE}, DEV
 * </pre>
 *
 * Means that subjects having the ADMIN.FR role from the realm(s) (like an LDAP directory) will be given the titi
 * local role within the FR scope only.
 *
 * @author yves.dautremay@mpsa.com
 * @author adrien.lauer@mpsa.com
 */
public class ConfigurationRoleMapping implements RoleMapping {

    /**
     * wildcard used to give role to every user
     */
    private final static String GLOBAL_WILDCARD = "*";

    /**
     * section name
     */
    public static final String ROLES_SECTION_NAME = "roles";

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationRoleMapping.class);

    /**
     * map : role = mapped roles
     */
    private final Map<String, Set<String>> map = new HashMap<String, Set<String>>();

    /**
     * roles given to every user
     */
    private final Set<String> givenRoles = new HashSet<String>();

    @Inject
    private Map<String, Class<? extends Scope>> scopeClasses;

    @Override
    public Collection<Role> resolveRoles(Set<String> auths, Collection<PrincipalProvider<?>> principalProviders) {
        Map<String, Role> roleMap = new HashMap<String, Role>();
        for (String role : givenRoles) {
            roleMap.put(role, new Role(role));
        }
        for (String auth : auths) {
            if (map.containsKey(auth)) {
                for (String roleName : map.get(auth)) {
                    if (!roleMap.containsKey(roleName)) {
                        roleMap.put(roleName, new Role(roleName));
                    }
                }
            } else {
                // maybe a scoped auth
                for (Map.Entry<String, Class<? extends Scope>> scopeClass : scopeClasses.entrySet()) {
                    for (String mapKey : map.keySet()) {
                        String wildcard = String.format("{%s}", scopeClass.getKey());


                        if (mapKey.contains(wildcard) && auth.matches(convertToRegex(mapKey, wildcard))) {
                            String scopeValue = findScope(wildcard, mapKey, auth);
                            Set<String> foundRoleNames = map.get(mapKey);

                            for (String foundRoleName : foundRoleNames) {
                                Role currentRole = getOrCreateRoleInMap(foundRoleName, roleMap);
                                Scope scope;

                                try {
                                    Constructor<? extends Scope> constructor = scopeClass.getValue().getConstructor(String.class);
                                    scope = constructor.newInstance(scopeValue);
                                } catch (Exception e) {
                                    throw SeedException.wrap(e, SecurityErrorCodes.UNABLE_TO_CREATE_SCOPE);
                                }

                                currentRole.getScopes().add(scope);
                            }
                        }
                    }
                }
            }
        }
        return roleMap.values();
    }

    private Role getOrCreateRoleInMap(String roleName, Map<String, Role> roleMap) {
        Role currentRole;
        if (roleMap.containsKey(roleName)) {
            currentRole = roleMap.get(roleName);
        } else {
            currentRole = new Role(roleName);
            roleMap.put(roleName, currentRole);
        }
        return currentRole;
    }

    /**
     * Read the configuration to init role mappings
     *
     * @param securityConfiguration configuration of security
     */
    @Inject
    public void readConfiguration(@Named("seed-security-config") Configuration securityConfiguration) {
        Configuration rolesConfiguration = securityConfiguration.subset(ROLES_SECTION_NAME);
        if (rolesConfiguration.isEmpty()) {
            LOGGER.warn("{} defined, but its configuration is empty.", getClass().getSimpleName());
            return;
        }
        map.clear();
        processRolesConfiguration(rolesConfiguration);
    }

    private void processRolesConfiguration(Configuration rolesConfiguration) {
        Iterator<String> keys = rolesConfiguration.getKeys();
        while (keys.hasNext()) {
            String roleName = keys.next();
            String[] perms = rolesConfiguration.getStringArray(roleName);
            for (String token : perms) {
                if (GLOBAL_WILDCARD.equals(token)) {
                    givenRoles.add(roleName);
                } else {
                    Set<String> roles = map.get(token);
                    if (roles == null) {
                        roles = new HashSet<String>();
                    }
                    roles.add(roleName);
                    map.put(token, roles);
                }
            }
        }
    }

    /**
     * Finds the scope in a string that corresponds to a role with {wildcard}.<br>
     * For example, if wildcardAuth is toto.{SCOPE} and auth is toto.foo then
     * scope is foo.
     *
     * @param wildcard     the wildcard to search for
     * @param wildcardAuth auth with {wildcard}
     * @param auth         auth that corresponds
     * @return the scope.
     */
    private String findScope(String wildcard, String wildcardAuth, String auth) {
        String scope;
        String before = StringUtils.substringBefore(wildcardAuth, wildcard);
        String after = StringUtils.substringAfter(wildcardAuth, wildcard);
        if (StringUtils.startsWith(wildcardAuth, wildcard)) {
            scope = StringUtils.substringBefore(auth, after);
        } else if (StringUtils.endsWith(wildcardAuth, wildcard)) {
            scope = StringUtils.substringAfter(auth, before);
        } else {
            scope = StringUtils.substringBetween(auth, before, after);
        }
        return scope;
    }

    private String convertToRegex(String value, String wildcard) {
        return String.format("\\Q%s\\E", value.replace(wildcard, "\\E.*\\Q"));
    }
}
