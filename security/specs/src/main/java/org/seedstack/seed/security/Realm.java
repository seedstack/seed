/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import org.seedstack.seed.security.principals.PrincipalProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A realm is used to authenticate and retrieve authorization for a user.
 */
public interface Realm {
    /**
     * @return the name of the realm
     */
    default String name() {
        return getClass().getSimpleName();
    }

    /**
     * Get the permissions
     *
     * @param identityPrincipal principal representing the identity of the user
     * @param otherPrincipals   other principals
     * @return the permissions of the user. Should not return null but empty
     * collection.
     */
    default Set<String> getRealmPermissions(PrincipalProvider<?> identityPrincipal, Collection<PrincipalProvider<?>> otherPrincipals) {
        return new HashSet<>();
    }

    /**
     * Get the roles
     *
     * @param identityPrincipal principal representing the identity of the user
     * @param otherPrincipals   other principals
     * @return the roles of the user. Should not return null but empty
     * collection.
     */
    default Set<String> getRealmRoles(PrincipalProvider<?> identityPrincipal, Collection<PrincipalProvider<?>> otherPrincipals) {
        return new HashSet<>();
    }

    /**
     * Authenticates the user and retrieves its properties in an
     * {@link AuthenticationInfo}
     *
     * @param token the credentials
     * @return the authentication of the user
     * @throws AuthenticationException if the user cannot be authenticated.
     */
    AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException;

    /**
     * Retrieves the RoleMapping associated to this realm
     *
     * @return the RoleMapping
     */
    RoleMapping getRoleMapping();

    /**
     * Retrieves the RolePermissionResolver associated to this realm
     *
     * @return the RolePermissionResolver
     */
    RolePermissionResolver getRolePermissionResolver();

    /**
     * Indicates the authentication token supported by this realm
     *
     * @return the class of AuthenticationToken supported
     */
    Class<? extends AuthenticationToken> supportedToken();
}
