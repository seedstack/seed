/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.guice;

/**
 * Utilities for Guice proxies.
 */
public final class ProxyUtils {
    private ProxyUtils() {
        // no instantiation allowed
    }

    /**
     * Tests if the class is a proxy.
     *
     * @param proxyClass The class to test.
     * @return true if class is proxy false otherwise.
     */
    public static boolean isProxy(Class<?> proxyClass) {
        return proxyClass.getName().contains("EnhancerByGuice");
    }

    /**
     * Return the non proxy class if needed.
     *
     * @param toClean The class to clean.
     * @return the cleaned class.
     */
    public static Class<?> cleanProxy(Class<?> toClean) {
        if (ProxyUtils.isProxy(toClean)) {
            return toClean.getSuperclass();
        }
        return toClean;
    }
}
