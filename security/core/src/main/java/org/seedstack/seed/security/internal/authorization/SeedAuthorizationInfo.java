/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.authorization;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.seedstack.seed.security.Role;
import org.seedstack.seed.security.Scope;

/**
 * AuthorizationInfo that keeps the Roles and Permissions for SeedStack API.
 */
public class SeedAuthorizationInfo implements AuthorizationInfo {
    private static final long serialVersionUID = 1L;
    private final Set<Role> apiRoles = new HashSet<>();
    private final Set<String> roles = new HashSet<>();
    private final Set<String> stringPermissions = new HashSet<>();
    private final Set<ScopePermission> scopePermissions = new HashSet<>();

    @Override
    public Collection<String> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    @Override
    public Collection<String> getStringPermissions() {
        return Collections.unmodifiableSet(stringPermissions);
    }

    @Override
    public Collection<Permission> getObjectPermissions() {
        return Collections.unmodifiableSet(scopePermissions);
    }

    /**
     * @return a Set of {@link Role}
     */
    public Set<Role> getObjectRoles() {
        return Collections.unmodifiableSet(apiRoles);
    }

    /**
     * Adds a role and its permissions to the authorization info.
     *
     * @param role the role to add.
     */
    public void addRole(Role role) {
        apiRoles.add(role);
        roles.add(role.getName());
        for (org.seedstack.seed.security.Permission permission : role.getPermissions()) {
            if (!role.getScopes().isEmpty()) {
                for (Scope scope : role.getScopes()) {
                    ScopePermission sp = new ScopePermission(permission.getPermission(), scope);
                    scopePermissions.add(sp);
                }
            } else {
                stringPermissions.add(permission.getPermission());
            }
        }
    }
}
