/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.authorization;

import org.apache.shiro.SecurityUtils;
import org.seedstack.seed.security.AuthorizationException;
import org.seedstack.seed.security.Logical;

import java.util.Arrays;


class AbstractInterceptor {

    protected void checkPermission(String permission) {
        try {
            SecurityUtils.getSubject().checkPermission(permission);
        } catch (org.apache.shiro.authz.AuthorizationException e) {
            throw new AuthorizationException("Subject doesn't have permission " + permission, e);
        }
    }

    protected boolean hasPermissions(String[] permissions, Logical logic) {
        switch (logic) {
            case AND:
                return hasAllPermissions(permissions);
            case OR:
                return hasAnyPermission(permissions);
            default:
                throw new AuthorizationException("Invalid logical operation specified");
        }

    }

    protected void checkRole(String role) {
        try {
            SecurityUtils.getSubject().checkRole(role);
        } catch (org.apache.shiro.authz.AuthorizationException e) {
            throw new AuthorizationException("Subject doesn't have role " + role, e);
        }
    }

    protected boolean hasRoles(String[] roles, Logical logic) {
        switch (logic) {
            case AND:
                return hasAllRoles(roles);
            case OR:
                return hasAnyRole(roles);
            default:
                throw new AuthorizationException("Invalid logical operation specified");
        }
    }

    protected boolean hasPermission(String permission) {
        return SecurityUtils.getSubject().isPermitted(permission);
    }

    protected boolean hasRole(String role) {
        return SecurityUtils.getSubject().hasRole(role);
    }

    private boolean hasAllPermissions(String[] permissions) {
        return SecurityUtils.getSubject().isPermittedAll(permissions);
    }

    private boolean hasAnyPermission(String[] permissions) {
        return Arrays.stream(permissions).anyMatch(this::hasPermission);
    }

    private boolean hasAllRoles(String[] roles) {
        return SecurityUtils.getSubject().hasAllRoles(Arrays.asList(roles));
    }

    private boolean hasAnyRole(String[] roles) {
        return Arrays.stream(roles).anyMatch(this::hasRole);
    }

}
