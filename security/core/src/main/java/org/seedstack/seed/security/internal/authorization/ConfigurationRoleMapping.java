/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal.authorization;

import static org.seedstack.shed.reflect.ReflectUtils.makeAccessible;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.security.Role;
import org.seedstack.seed.security.RoleMapping;
import org.seedstack.seed.security.Scope;
import org.seedstack.seed.security.SecurityConfig;
import org.seedstack.seed.security.internal.SecurityErrorCode;
import org.seedstack.seed.security.principals.PrincipalProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolve the role mappings from a Configuration:
 *
 * <pre>
 *     admin = ADMIN, DEV
 * </pre>
 * <p>
 * Means that the admin local role will be given to any identified subject to which the realm(s) have given the ADMIN
 * <strong>or</strong> DEV role.
 * <p>
 * Roles can be given automatically to any user (whatever their real authorizations given by the realms) by using
 * the '*' wildcard:
 *
 * <pre>
 *     reader = *
 * </pre>
 * <p>
 * Means that every identified subject will have the reader role.
 * <p>
 * This implementation handles simple scopes:
 *
 * <pre>
 *     admin = ADMIN.{SCOPE}, DEV
 * </pre>
 * <p>
 * Means that subjects having the ADMIN.FR role from the realm(s) (like an LDAP directory) will be given the titi
 * local role within the FR scope only.
 */
public class ConfigurationRoleMapping implements RoleMapping {
    private final static String GLOBAL_WILDCARD = "*";
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationRoleMapping.class);
    private final Map<String, Set<String>> map = new HashMap<>();
    private final Set<String> everybodyRoles = new HashSet<>();

    @Inject
    private Map<String, Class<? extends Scope>> scopeClasses;

    @Override
    public Collection<Role> resolveRoles(Set<String> auths, Collection<PrincipalProvider<?>> principalProviders) {
        Map<String, Role> roleMap = new HashMap<>();
        for (String role : everybodyRoles) {
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
                    for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
                        String mapKey = entry.getKey();
                        String wildcard = String.format("{%s}", scopeClass.getKey());

                        if (mapKey.contains(wildcard) && auth.matches(convertToRegex(mapKey, wildcard))) {
                            String scopeValue = findScope(wildcard, mapKey, auth);

                            for (String foundRoleName : entry.getValue()) {
                                Scope scope;

                                try {
                                    Constructor<? extends Scope> constructor = scopeClass.getValue().getConstructor(
                                            String.class);
                                    makeAccessible(constructor);
                                    scope = constructor.newInstance(scopeValue);
                                } catch (IllegalAccessException | InstantiationException | InvocationTargetException
                                        | NoSuchMethodException e) {
                                    throw SeedException.wrap(e, SecurityErrorCode.UNABLE_TO_CREATE_SCOPE).put(
                                            "scopeName", scopeClass.getValue().getName());
                                }

                                getOrCreateRoleInMap(foundRoleName, roleMap).getScopes().add(scope);
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

    @Inject
    void readConfiguration(SecurityConfig securityConfig) {
        if (securityConfig.getRoles().isEmpty()) {
            LOGGER.warn("{} defined, but its configuration is empty.", getClass().getSimpleName());
            return;
        }

        map.clear();

        for (Map.Entry<String, Set<String>> entry : securityConfig.getRoles().entrySet()) {
            for (String permission : entry.getValue()) {
                if (GLOBAL_WILDCARD.equals(permission)) {
                    everybodyRoles.add(entry.getKey());
                } else {
                    Set<String> roles = map.get(permission);
                    if (roles == null) {
                        roles = new HashSet<>();
                    }
                    roles.add(entry.getKey());
                    map.put(permission, roles);

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
        String before = substringBefore(wildcardAuth, wildcard);
        String after = substringAfter(wildcardAuth, wildcard);
        if (wildcardAuth.startsWith(wildcard)) {
            scope = substringBefore(auth, after);
        } else if (wildcardAuth.endsWith(wildcard)) {
            scope = substringAfter(auth, before);
        } else {
            scope = substringBetween(auth, before, after);
        }
        return scope;
    }

    private String convertToRegex(String value, String wildcard) {
        return String.format("\\Q%s\\E", value.replace(wildcard, "\\E.*\\Q"));
    }

    private String substringBefore(String str, String sep) {
        int idx = str.indexOf(sep);
        if (idx == -1) {
            return str;
        } else {
            return str.substring(0, idx);
        }
    }

    private String substringAfter(String str, String sep) {
        int idx = str.indexOf(sep);
        if (idx == -1) {
            return str;
        } else {
            return str.substring(idx + sep.length());
        }
    }

    private String substringBetween(String str, String start, String end) {
        String result = str;
        int idx = str.indexOf(start);
        if (idx != -1) {
            result = result.substring(idx + start.length());
        }
        idx = result.indexOf(end);
        if (idx != -1) {
            result = result.substring(0, idx);
        }
        return result;
    }
}
