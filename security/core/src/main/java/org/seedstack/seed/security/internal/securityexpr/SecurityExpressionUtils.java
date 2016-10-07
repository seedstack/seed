/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.securityexpr;

import org.seedstack.seed.security.Scope;
import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.security.SimpleScope;

import javax.inject.Inject;

/**
 * This class is an entry point for the security expression language.
 * <p>
 * It is mainly a static gateway to {@link SecuritySupport}.
 * <p>
 * It is not meant to be used by projects directly.
 */
public final class SecurityExpressionUtils {

    @Inject
    private static SecuritySupport securitySupport;

    private SecurityExpressionUtils() {
    }

    /**
     * Checks the current user role.
     *
     * @param role the role to check
     * @return true if user has the given role
     */
    public static boolean hasRole(String role) {
        return securitySupport.hasRole(role);
    }

    /**
     * Checks if the current user has at least one of the given roles.
     *
     * @param roles the list of role to check
     * @return true if user has the one of the given roles
     */
    public static boolean hasOneRole(String... roles) {
        return securitySupport.hasAnyRole(roles);
    }

    /**
     * Checks the current user roles.
     *
     * @param roles the list of role to check
     * @return true if user has all the given roles
     */
    public static boolean hasAllRoles(String... roles) {
        return securitySupport.hasAllRoles(roles);
    }

    /**
     * Checks the current user role in the given scopes.
     *
     * @param role the role to check
     * @param simpleScopes the list of simple scopes to verify the role on (optional).
     * @return true if the user has the role for all the given simple scopes.
     */
    public static boolean hasRole(String role, String... simpleScopes) {
        return securitySupport.hasRole(role, getSimpleScopes(simpleScopes));
    }

    /**
     * Checks the current user permission.
     *
     * @param permission the permission to check
     * @return true if user has the given permission
     */
    public static boolean hasPermission(String permission) {
        return securitySupport.isPermitted(permission);
    }

    /**
     * Checks if the current user has at least one of the given permissions.
     *
     * @param permissions the list of permission to check
     * @return true if user has at least one of the permissions
     */
    public static boolean hasOnePermission(String... permissions) {
        return securitySupport.isPermittedAny(permissions);
    }

    /**
     * Checks the current user permissions.
     *
     * @param permissions the list of permission to check
     * @return true if user has all the given permissions
     */
    public static boolean hasAllPermissions(String... permissions) {
        return securitySupport.isPermittedAll(permissions);
    }

    /**
     * Checks the current user permission.
     *
     * @param permission the permission to check
     * @param simpleScopes the list of simple scopes for this permission
     * @return true if user has the given permission
     */
    public static boolean hasPermission(String permission, String... simpleScopes) {
        return securitySupport.isPermitted(permission, getSimpleScopes(simpleScopes));
    }

    private static Scope[] getSimpleScopes(String[] scopeValues) {
        Scope[] scopes = new Scope[scopeValues.length];
        for (int i = 0; i < scopes.length; i++) {
            scopes[i] = new SimpleScope(scopeValues[i]);
        }
        return scopes;
    }


}
