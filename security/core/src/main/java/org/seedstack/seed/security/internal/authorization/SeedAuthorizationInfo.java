/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
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
 * AuthorizationInfo that keeps the Roles and Permissions from SEED api.
 * 
 * @author yves.dautremay@mpsa.com
 * 
 */
public class SeedAuthorizationInfo implements AuthorizationInfo {

    private static final long serialVersionUID = 8949548650667096378L;

    private Set<Role> apiRoles = new HashSet<Role>();

    private Set<String> roles = new HashSet<String>();

    private Set<String> stringPermissions = new HashSet<String>();

    private Set<Permission> objectPermissions = new HashSet<Permission>();

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
        return Collections.unmodifiableSet(objectPermissions);
    }

    /**
     * Gives the SEED roles
     * 
     * @return a Set of {@link Role}
     */
    public Set<Role> getSeedRoles() {
        return Collections.unmodifiableSet(apiRoles);
    }

    /**
     * Adds a role and its permissions
     * 
     * @param role
     *            the role to add
     */
    public void addRole(Role role) {
        apiRoles.add(role);
        roles.add(role.getName());
        for (org.seedstack.seed.security.Permission permission : role.getPermissions()) {
            if (!role.getScopes().isEmpty()) {
                for (Scope scope : role.getScopes()) {
                    ScopePermission sp = new ScopePermission(permission.getPermission(), scope);
                    objectPermissions.add(sp);
                }
            }else{
                stringPermissions.add(permission.getPermission());
            }
        }
    }
}
