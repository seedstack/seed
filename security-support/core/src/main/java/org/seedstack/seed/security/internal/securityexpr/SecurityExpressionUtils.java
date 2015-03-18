/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.securityexpr;

import org.seedstack.seed.security.api.Domain;
import org.seedstack.seed.security.api.Scope;
import org.seedstack.seed.security.api.SecuritySupport;

import javax.inject.Inject;

/**
 * This class is an entry point for the security expression language.
 * <p/>
 * It is mainly a static gateway to {@link SecuritySupport}.
 * <p/>
 * It is not meant to be used by projects directly.
 *
 * @author epo.jemba@ext.mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
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
     * Checks the current user role in the given domains.
     *
     * @param role    the role to check
     * @param domains the list of domains
     * @return true if the user has the role for all the given domains.
     */
    public static boolean hasRole(String role, String... domains) {
        return securitySupport.hasRole(role, getScopes(domains));
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
     * @param domains the list of domains for this permission
     * @return true if user has the given permission
     */
    public static boolean hasPermission(String permission, String... domains) {
        return securitySupport.isPermitted(permission, getScopes(domains));
    }

    private static Scope[] getScopes(String[] domains) {
        Scope[] scopes = new Scope[domains.length];
        for (int i = 0; i < scopes.length; i++) {
            scopes[i] = new Domain(domains[i]);
        }
        return scopes;
    }


}
