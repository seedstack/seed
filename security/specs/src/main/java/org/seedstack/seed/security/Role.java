/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Role and optionally a collection of scopes on which it is
 * given. This scope must be given to all the permissions this role grants.
 */
public class Role implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final Collection<Scope> scopes = new ArrayList<>();
    private final Set<Permission> permissions = new HashSet<>();

    /**
     * Constructor with name
     *
     * @param name the name
     */
    public Role(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the scopes
     */
    public Collection<Scope> getScopes() {
        return scopes;
    }

    /**
     * @return the permissions
     */
    public Set<Permission> getPermissions() {
        return permissions;
    }

    /**
     * Filters the scopes corresponding to a type
     *
     * @param <S>       the type of the scope to filter.
     * @param scopeType the type of scope
     * @return the scopes of the given type
     */
    @SuppressWarnings("unchecked")
    public <S extends Scope> Set<S> getScopesByType(Class<S> scopeType) {
        Set<S> typedScopes = new HashSet<>();
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
     * Convenient method to get an unmodifiable role from a role
     *
     * @param role the role to protect
     * @return the unmodifiable role
     */
    public static Role unmodifiableRole(Role role) {
        return new UnmodifiableRole(role);
    }

    private static final class UnmodifiableRole extends Role {
        private static final long serialVersionUID = 1L;

        private UnmodifiableRole(Role role) {
            super(role.getName());
            super.scopes.addAll(role.getScopes());
            super.permissions.addAll(role.getPermissions());
        }

        @Override
        public Collection<Scope> getScopes() {
            return Collections.unmodifiableCollection(super.getScopes());
        }

        @Override
        public Set<Permission> getPermissions() {
            return Collections.unmodifiableSet(super.getPermissions());
        }
    }
}
