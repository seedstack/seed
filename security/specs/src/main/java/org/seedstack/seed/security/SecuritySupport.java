/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import org.seedstack.seed.security.principals.PrincipalProvider;
import org.seedstack.seed.security.principals.SimplePrincipalProvider;

/**
 * Support for all security concerns. Retrieve connected user principals, get/check roles/permissions...
 */
public interface SecuritySupport {

    /**
     * Gets the principal provider that holds the user's identity. The identity is just an Object; in most simple
     * case, a String of the user's id.
     *
     * @return The principalProvider holding the identity. Not null, empty value if none.
     */
    PrincipalProvider<?> getIdentityPrincipal();

    /**
     * Retrieves all the PrincipalProviders containing the user's details.
     *
     * @return A non-null collection of user's PrincipalProviders.
     */
    Collection<PrincipalProvider<?>> getOtherPrincipals();

    /**
     * Gets all the PrincipalProviders corresponding to a type of PrincipalProvider.<br>
     * <br>
     * For example, you can use this method to get the LDAPUser by calling :<br>
     * {@code getPrincipalsByType(LDAPUser.class)}.<br>
     * <br>
     * Then on the first element of the collection : <br>
     * {@code LDAPUser user =
     * ldapUserPrincipalProvider.getPrincipal()}.
     *
     * @param <T>            type of the principal
     * @param principalClass the Principal type, not null
     * @return A collection of the user's PrincipalProviders of type principalProviderClass. Not null.
     * @see org.seedstack.seed.security.principals.Principals#getPrincipalsByType(Collection, Class)
     */
    <T extends Serializable> Collection<PrincipalProvider<T>> getPrincipalsByType(Class<T> principalClass);

    /**
     * Gets the user's SimplePrincipalProviders.<br>
     * A {@link org.seedstack.seed.security.principals.SimplePrincipalProvider} is a name/value principal. A list of
     * common SimplePrincipalProviders names are found in class
     * {@link org.seedstack.seed.security.principals.Principals}.
     *
     * @return the list of the user's SimplePrincipalProviders. Not null
     * @see org.seedstack.seed.security.principals.Principals#getSimplePrincipals(Collection)
     */
    Collection<SimplePrincipalProvider> getSimplePrincipals();

    /**
     * Gets the SimplePrincipalProvider which name is provided.
     *
     * @param principalName the name of the principal. Null returns null.
     * @return the SimplePrincipalProvider identified by principalName. Null if none found.
     * @see org.seedstack.seed.security.principals.Principals#getSimplePrincipalByName(Collection, String)
     */
    SimplePrincipalProvider getSimplePrincipalByName(String principalName);

    /**
     * Tells if the connected user has the given permission.<br>
     * The permission is given as a String in the form "object:action[:id]" (e.g. door:open or document:print).
     *
     * @param permission the string permission to test. Not null
     * @return true if user has the given permission, false otherwise.
     */
    boolean isPermitted(String permission);

    /**
     * Tells if the connected user has the given permission on the given scopes.<br>
     * The permission is given as a String in the form "object:action[:id]" (e.g. door:open or document:print).
     *
     * @param permission the string permission to test. Not null
     * @param scopes     the scopes to verify the permission on. optional
     * @return true if user has the given permission, false otherwise.
     */
    boolean isPermitted(String permission, Scope... scopes);

    /**
     * Tells if the connected user has all of the given permissions.<br>
     * The permissions are given as Strings in the form "object:action[:id]" (e.g. door:open or document:print).
     *
     * @param permissions the string permissions to test. not null
     * @return true if user has all the given permissions, false otherwise.
     */
    boolean isPermittedAll(String... permissions);

    /**
     * Tells if the connected user has at least one of the given permissions.<br>
     * The permissions are given as Strings in the form "object:action[:id]" (e.g. door:open or document:print).
     *
     * @param permissions the string permissions to test. Not null
     * @return true if user has at least one of the given permissions, false otherwise.
     */
    boolean isPermittedAny(String... permissions);

    /**
     * Checks if the connected user has the given permission on the given scopes : if the user does not have the
     * permission, throws an exception to
     * block execution.<br>
     * <br>
     * The permission is given as a String in the form "object:action[:id]" (e.g. door:open or document:print).
     *
     * @param permission the string permission to test. Not null.
     * @param scopes     the scopes to verify the permission on. optional
     * @throws AuthorizationException if the user does not have the permission.
     */
    void checkPermission(String permission, Scope... scopes);

    /**
     * Checks if the connected user has the given permission : if the user does not have the permission, throws an
     * exception to block execution.<br>
     * <br>
     * The permission is given as a String in the form "object:action[:id]" (e.g. door:open or document:print).
     *
     * @param permission the string permission to test. Not null.
     * @throws AuthorizationException if the user does not have the permission.
     */
    void checkPermission(String permission);

    /**
     * Checks if the connected user has the given permissions : if the user does not have all the permissions, throws
     * an exception to block execution.<br>
     * <br>
     * The permissions are given as Strings in the form "object:action[:id]" (e.g. door:open or document:print).
     *
     * @param permissions the string permissions to test. Not null.
     * @throws AuthorizationException if the user does not have all of the given permissions.
     */
    void checkPermissions(String... permissions);

    /**
     * Tells if the connected user has the given role on all the given scopes.<br>
     * <br>
     * Note that it is discouraged to test for roles in your application as role names are prone to change in the
     * life span of your application. To
     * ensure durability of your code, check permissions instead of roles.
     *
     * @param roleIdentifier the id of the role to test. Not null.
     * @param scopes         the scopes to verify the role on. optional
     * @return true if the user has the role, false otherwise.
     */
    boolean hasRole(String roleIdentifier, Scope... scopes);

    /**
     * Tells if the connected user has the given role.<br>
     * <br>
     * Note that it is discouraged to test for roles in your application as role names are prone to change in the
     * life span of your application. To
     * ensure durability of your code, check permissions instead of roles.
     *
     * @param roleIdentifier the id of the role to test. Not null.
     * @return true if the user has the role, false otherwise.
     */
    boolean hasRole(String roleIdentifier);

    /**
     * Tells if the connected user has all of the given roles.<br>
     * <br>
     * Note that it is discouraged to test for roles in your application as role names are prone to change in the
     * life span of your application. To
     * ensure durability of your code, check permissions instead of roles.
     *
     * @param roleIdentifiers the names of the roles to test. Not null.
     * @return true if the user has all the roles, false otherwise.
     */
    boolean hasAllRoles(String... roleIdentifiers);

    /**
     * Tells if the connected user has at least one of the given roles.<br>
     * <br>
     * Note that it is discouraged to test for roles in your application as role names are prone to change in the
     * life span of your application. To
     * ensure durability of your code, check permissions instead of roles.
     *
     * @param roleIdentifiers the names of the roles to test. Not null.
     * @return true if the user has at least one of the roles, false otherwise.
     */
    boolean hasAnyRole(String... roleIdentifiers);

    /**
     * Checks if the connected user has the given role : throws an exception otherwise.<br>
     * <br>
     * Note that it is discouraged to test for roles in your application as role names are prone to change in the
     * life span of your application. To
     * ensure durability of your code, check permissions instead of roles.
     *
     * @param roleIdentifier the name of the role to check. Not null.
     * @throws AuthorizationException if the user does not have the given role.
     */
    void checkRole(String roleIdentifier);

    /**
     * Checks if the connected user has all the given roles : throws an exception otherwise.<br>
     * <br>
     * Note that it is discouraged to test for roles in your application as role names are prone to change in the
     * life span of your application. To
     * ensure durability of your code, check permissions instead of roles.
     *
     * @param roleIdentifiers the name of the roles to check. Not null.
     * @throws AuthorizationException if the user does not have all the given role.
     */
    void checkRoles(String... roleIdentifiers);

    /**
     * Gives the roles given to the user. You can find all the scopes of a Role by calling role.getScopes() or the
     * scopes
     * of a specific type (like SimpleScope) by calling role.getScopesByType(SimpleScope.class)
     *
     * @return a Set of all the roles the user has. Not null, empty if none.
     */
    Set<Role> getRoles();

    /**
     * Gives all the simple scopes of the user found in all its roles.
     *
     * @return a Set of all the scopes.
     */
    Set<SimpleScope> getSimpleScopes();

    /**
     * Logs out the connected user and invalidates and/or removes any associated entities, such as a Session and
     * authorization data. After this method
     * is called, the user is considered 'anonymous' and may continue to be used for another log-in if desired.
     * <h3>Web Environment Warning</h3>
     * Calling this method in web environments will usually remove any associated session cookie as part of session
     * invalidation. Because cookies are
     * part of the HTTP header, and headers can only be set before the response body (html, image, etc) is sent, this
     * method in web environments must
     * be called before <em>any</em> content has been rendered.
     * <p>
     * The typical approach most applications use in this scenario is to redirect the user to a different location (e
     * .g. home page) immediately after
     * calling this method. This is an effect of the HTTP protocol itself and not a reflection of the implementation.
     */
    void logout();

    /**
     * Check if the current user is authenticated.
     *
     * Authenticated on Shiro means that subject has successfully logged in on the current session
     *
     * @return true if authenticated, false otherwise.
     * @see org.seedstack.seed.security.SecuritySupport#isRemembered()
     */
    boolean isAuthenticated();

    /**
     * Checks if the current user has logged successfully on a previous session
     *
     * @return true if remembered, false otherwise.
     */
    boolean isRemembered();

    /**
     * Returns the host name or IP string of the host of the connected user, or {@code null} if the host is unknown.
     *
     * @return the host name or IP string of the host that originated this session, or {@code null} if the host
     * address is unknown.
     */
    String getHost();
}
