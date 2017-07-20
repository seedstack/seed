/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.principals;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Utility class to create and manipulate common principals.
 */
public final class Principals {
    public static final String IDENTITY = "userId";
    public static final String LOCALE = "locale";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String FULL_NAME = "fullName";

    private Principals() {
        // no instantiation allowed
    }

    private static SimplePrincipalProvider simplePrincipal(String name, String value) {
        return new SimplePrincipalProvider(name, value);
    }

    /**
     * Simple principal to store the identity as a string
     *
     * @param identity the identity
     * @return the built principal
     */
    public static SimplePrincipalProvider identityPrincipal(String identity) {
        return simplePrincipal(IDENTITY, identity);
    }

    /**
     * Simple principal to store the locale as a string
     *
     * @param locale the locale
     * @return the built principal
     */
    public static SimplePrincipalProvider localePrincipal(String locale) {
        return simplePrincipal(LOCALE, locale);
    }

    /**
     * Simple principal to store the firstName as a string
     *
     * @param firstName the firstName
     * @return the built principal
     */
    public static SimplePrincipalProvider firstNamePrincipal(String firstName) {
        return simplePrincipal(FIRST_NAME, firstName);
    }

    /**
     * Simple principal to store the lastName as a string
     *
     * @param lastName the lastName
     * @return the built principal
     */
    public static SimplePrincipalProvider lastNamePrincipal(String lastName) {
        return simplePrincipal(LAST_NAME, lastName);
    }

    /**
     * Simple principal to store the fullName as a string
     *
     * @param fullName the fullName
     * @return the built principal
     */
    public static SimplePrincipalProvider fullNamePrincipal(String fullName) {
        return simplePrincipal(FULL_NAME, fullName);
    }

    /**
     * Gets all the PrincipalProviders corresponding to a type of PrincipalProvider in a collection.<br>
     * <br>
     * For example, you can use this method to get the LDAPUser by calling :<br>
     * <code>getPrincipalsByType(principals, LDAPUserPrincipalProvider.class)</code> .<br>
     * <br>
     * Then on the first element of the collection : <br>
     * <code>LDAPUser user =
     * ldapUserPrincipalProvider.getPrincipal()</code>.
     *
     * @param <T>                type of the PrincipalProvider
     * @param principalProviders the principals to find the type.
     * @param principalClass     the PrincipalProvider type, not null
     * @return A collection of the user's PrincipalProviders of type principalProviderClass. Not null.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> Collection<PrincipalProvider<T>> getPrincipalsByType(Collection<PrincipalProvider<?>> principalProviders, Class<T> principalClass) {
        Collection<PrincipalProvider<T>> principals = new ArrayList<>();
        for (PrincipalProvider<?> principal : principalProviders) {
            for (Type principalInterface : principal.getClass().getGenericInterfaces()) {
                if (principalInterface instanceof ParameterizedType) {
                    ParameterizedType currentPrincipalClass = (ParameterizedType) principalInterface;
                    Type currentType = currentPrincipalClass.getActualTypeArguments()[0];
                    if (!principalClass.isArray()) {
                        if (principalClass.equals(currentType)) {
                            principals.add((PrincipalProvider<T>) principal);
                        }
                    } else if (currentType instanceof Class && principalClass.getComponentType().equals(((Class<?>) currentType).getComponentType())) {
                        principals.add((PrincipalProvider<T>) principal);
                    }
                }
            }
        }
        return principals;
    }

    /**
     * Gets one PrincipalProvider corresponding to a type of PrincipalProvider.<br>
     * <br>
     * For example, you can use this method to get the LDAPUser by calling :<br>
     * <code>getOnePrincipalsByType(principals, LDAPUserPrincipalProvider.class)</code> .<br>
     * <br>
     * Then : <br>
     * <code>LDAPUser user =
     * ldapUserPrincipalProvider.getPrincipal()</code>.
     *
     * @param <T>                type of the PrincipalProvider
     * @param principalProviders the principals to find the type.
     * @param principalClass     the PrincipalProvider type, not null
     * @return The user's PrincipalProvider of type principalProviderClass. Null if none.
     */
    public static <T extends Serializable> PrincipalProvider<T> getOnePrincipalByType(Collection<PrincipalProvider<?>> principalProviders, Class<T> principalClass) {
        Collection<PrincipalProvider<T>> pps = getPrincipalsByType(principalProviders, principalClass);
        if (!pps.isEmpty()) {
            return pps.iterator().next();
        } else {
            return null;
        }
    }

    /**
     * Extracts the simple principals of the collection of principals
     *
     * @param principalProviders the principals to extract from
     * @return the simple principals
     */
    public static Collection<SimplePrincipalProvider> getSimplePrincipals(Collection<PrincipalProvider<?>> principalProviders) {
        Collection<SimplePrincipalProvider> principals = new ArrayList<>();
        for (PrincipalProvider<?> principal : principalProviders) {
            if (principal instanceof SimplePrincipalProvider) {
                principals.add((SimplePrincipalProvider) principal);
            }
        }
        return principals;
    }

    /**
     * Gives the simple principal with the given name from the given collection of principals
     *
     * @param principalProviders the principals to search
     * @param principalName      the name to search
     * @return the simple principal with the name
     */
    public static SimplePrincipalProvider getSimplePrincipalByName(Collection<PrincipalProvider<?>> principalProviders, String principalName) {
        for (SimplePrincipalProvider principal : getSimplePrincipals(principalProviders)) {
            if (principal.getName().equals(principalName)) {
                return principal;
            }
        }
        return null;
    }
}
