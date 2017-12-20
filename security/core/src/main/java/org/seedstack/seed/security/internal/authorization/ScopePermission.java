/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal.authorization;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.seedstack.seed.security.Scope;

/**
 * Represents a permission given on a limited scope
 */
public class ScopePermission implements Permission {

    private String permission;

    private Scope scope;

    /**
     * Constructor with a permission
     *
     * @param permission the permission
     */
    public ScopePermission(String permission) {
        this.permission = permission;
    }

    /**
     * Constructor with permissions and scope.
     *
     * @param permission the permission
     * @param scope      the scope
     */
    public ScopePermission(String permission, Scope scope) {
        this.permission = permission;
        this.scope = scope;
    }

    @Override
    public boolean implies(Permission p) {
        if (scope != null && p instanceof ScopePermission) {
            ScopePermission sp = (ScopePermission) p;
            return scope.includes(sp.getScope()) && buildWildcardPermission().implies(sp.buildWildcardPermission());
        } else {
            return buildWildcardPermission().implies(p);
        }
    }

    private WildcardPermission buildWildcardPermission() {
        return new WildcardPermission(permission);
    }

    /**
     * Getter permission
     *
     * @return the permission
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Getter scopes
     *
     * @return the scopes
     */
    public Scope getScope() {
        return scope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ScopePermission that = (ScopePermission) o;

        return permission.equals(that.permission) && scope.equals(that.scope);
    }

    @Override
    public int hashCode() {
        int result = permission.hashCode();
        result = 31 * result + scope.hashCode();
        return result;
    }
}
