/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Role and optionnally a collection of scopes on which it is
 * given. This scope must be given to all the permissions this role grants.
 * 
 * @author yves.dautremay@mpsa.com
 */
public class Role {

    /**
     * name of the role
     */
    private String name;

    /**
     * scopes
     */
    private Collection<Scope> scopes;

    /** permissions */
    private Set<Permission> permissions;

    /**
     * Constructor with name
     * 
     * @param name
     *            the name
     */
    public Role(String name) {
        this.name = name;
        scopes = new ArrayList<Scope>();
        permissions = new HashSet<Permission>();
    }

    /**
     * Getter name
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter scopes
     * 
     * @return the scopes
     */
    public Collection<Scope> getScopes() {
        return scopes;
    }

    /**
     * Getter permissions
     * 
     * @return the permissions
     */
    public Set<Permission> getPermissions() {
        return permissions;
    }

    /**
     * Filters the scopes corresponding to a type
     * 
     * @param scopeType
     *            the type of scope
     * @return the scopes of the given type
     */
    @SuppressWarnings("unchecked")
    public <S extends Scope> Set<S> getScopesByType(Class<S> scopeType) {
        Set<S> typedScopes = new HashSet<S>();
        for (Scope scope : getScopes()) {
            if (scopeType.isInstance(scope)) {
                typedScopes.add((S) scope);
            }
        }
        return typedScopes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Role role = (Role) o;

        return name.equals(role.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Convinient method to get an unmodifiable role from a role
     * 
     * @param role
     *            the role to protect
     * @return the unmodifiable role
     */
    public static Role unmodifiableRole(Role role) {
        return new UnmodifiableRole(role);
    }

    private static final class UnmodifiableRole extends Role {

        private Role role;

        private UnmodifiableRole(Role role) {
            super(role.getName());
            this.role = role;
        }

        @Override
        public Collection<Scope> getScopes() {
            return Collections.unmodifiableCollection(role.getScopes());
        }

        @Override
        public Set<Permission> getPermissions() {
            return Collections.unmodifiableSet(role.getPermissions());
        }
    }
}
