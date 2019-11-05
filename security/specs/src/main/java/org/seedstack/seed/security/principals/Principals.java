/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.principals;

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
     * Gets all the {@link PrincipalProvider}s that provide a principal assignable to the specified type.
     *
     * @param <T>                type of the PrincipalProvider
     * @param principalProviders the principals to find the type.
     * @param principalClass     the PrincipalProvider type, not null
     * @return A collection of the user's PrincipalProviders of type principalProviderClass. Not null.
     */
    public static <T> Collection<PrincipalProvider<T>> getPrincipalsByType(
            Collection<PrincipalProvider<?>> principalProviders, Class<T> principalClass) {
        return getPrincipalsByType(principalProviders, principalClass, -1);
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
    public static <T> PrincipalProvider<T> getOnePrincipalByType(
            Collection<PrincipalProvider<?>> principalProviders, Class<T> principalClass) {
        Collection<PrincipalProvider<T>> pps = getPrincipalsByType(principalProviders, principalClass, 1);
        if (!pps.isEmpty()) {
            return pps.iterator().next();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Collection<PrincipalProvider<T>> getPrincipalsByType(
            Collection<PrincipalProvider<?>> principalProviders, Class<T> principalClass, int limit) {
        Collection<PrincipalProvider<T>> principals = new ArrayList<>();
        for (PrincipalProvider<?> principal : principalProviders) {
            for (Type principalInterface : principal.getClass().getGenericInterfaces()) {
                if (limit >= 0 && principals.size() >= limit) {
                    return principals;
                }

                if (principalInterface instanceof ParameterizedType && ((ParameterizedType) principalInterface).getRawType()
                        .equals(PrincipalProvider.class)) {
                    Type currentType = ((ParameterizedType) principalInterface).getActualTypeArguments()[0];
                    if (!principalClass.isArray()) {
                        if (principalClass.isAssignableFrom((Class<?>) currentType)) {
                            principals.add((PrincipalProvider<T>) principal);
                        }
                    } else if (currentType instanceof Class) {
                        Class<?> componentType = ((Class<?>) currentType).getComponentType();
                        if (componentType != null && principalClass.getComponentType()
                                .isAssignableFrom(componentType)) {
                            principals.add((PrincipalProvider<T>) principal);
                        }
                    }
                }
            }
        }
        return principals;
    }

    /**
     * Extracts the simple principals of the collection of principals
     *
     * @param principalProviders the principals to extract from
     * @return the simple principals
     */
    public static Collection<SimplePrincipalProvider> getSimplePrincipals(
            Collection<PrincipalProvider<?>> principalProviders) {
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
    public static SimplePrincipalProvider getSimplePrincipalByName(Collection<PrincipalProvider<?>> principalProviders,
            String principalName) {
        for (SimplePrincipalProvider principal : getSimplePrincipals(principalProviders)) {
            if (principal.getName().equals(principalName)) {
                return principal;
            }
        }
        return null;
    }
}
