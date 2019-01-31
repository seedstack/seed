/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.securityexpr;

import javax.inject.Inject;
import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.security.SimpleScope;

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
     * Checks the current user role in the given scopes.
     *
     * @param role        the role to check
     * @param simpleScope the simple scope to check this role on.
     * @return true if the user has the role for the given simple scope.
     */
    public static boolean hasRoleOn(String role, String simpleScope) {
        return securitySupport.hasRole(role, new SimpleScope(simpleScope));
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
     * Checks the current user permission.
     *
     * @param permission  the permission to check
     * @param simpleScope the simple scope to check this permission on.
     * @return true if user has the given permission for the given simple scope.
     */
    public static boolean hasPermissionOn(String permission, String simpleScope) {
        return securitySupport.isPermitted(permission, new SimpleScope(simpleScope));
    }
}
