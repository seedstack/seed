/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal.authorization;

import java.io.Serializable;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.seedstack.seed.security.Scope;

/**
 * Represents a permission given on a limited scope
 */
public class ScopePermission implements Permission, Serializable {
    private static final long serialVersionUID = 1L;
    private final WildcardPermission permission;
    private final Scope scope;

    /**
     * Constructor with a permission
     *
     * @param permission the permission
     */
    public ScopePermission(String permission) {
        this.permission = new WildcardPermission(permission);
        this.scope = null;
    }

    /**
     * Constructor with permissions and scope.
     *
     * @param permission the permission
     * @param scope      the scope
     */
    public ScopePermission(String permission, Scope scope) {
        this.permission = new WildcardPermission(permission);
        this.scope = scope;
    }

    @Override
    public boolean implies(Permission p) {
        if (scope != null && p instanceof ScopePermission) {
            ScopePermission sp = (ScopePermission) p;
            return scope.includes(sp.getScope()) && permission.implies(sp.permission);
        } else {
            return permission.implies(p);
        }
    }

    /**
     * Getter permission
     *
     * @return the permission
     */
    public String getPermission() {
        return permission.toString();
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
