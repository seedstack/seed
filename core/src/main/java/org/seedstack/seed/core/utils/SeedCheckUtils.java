/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import org.kametic.specifications.Specification;
import org.seedstack.seed.ErrorCode;
import org.seedstack.seed.SeedException;

import javax.inject.Inject;

/**
 * Class with various check utilities.
 */
public final class SeedCheckUtils {
    @Inject
    private static Injector injector;

    private SeedCheckUtils() {
    }

    /**
     * Checks if the specified instance is null. If not, throws a SeedException with NULL_CHECK_FAILED error code.
     *
     * @param actual     the instance to check
     * @param properties the exception properties
     */
    public static void checkIfNull(Object actual, String... properties) {
        if (actual != null) {
            throwSeedException(CoreUtilsErrorCode.NULL_CHECK_FAILED, null, properties);
        }
    }

    /**
     * Checks if the specified instance is not null. If it is, throws a SeedException with NOT_NULL_CHECK_FAILED error
     * code.
     *
     * @param actual     the instance to check
     * @param properties the exception properties
     */
    public static void checkIfNotNull(Object actual, String... properties) {
        if (actual == null) {
            throwSeedException(CoreUtilsErrorCode.NOT_NULL_CHECK_FAILED, null, properties);
        }
    }

    /**
     * Checks if the specified boolean expression is true. If not, throws a SeedException with CHECK_FAILED error code;
     *
     * @param check      the check to assess
     * @param properties the exception properties
     */
    public static void checkIf(boolean check, String... properties) {
        if (!check) {
            throwSeedException(CoreUtilsErrorCode.CHECK_FAILED, null, properties);
        }
    }

    /**
     * Checks if the specified class is injectable. If not, throws a SeedException with INJECTABLE_CHECK_FAILED error code.
     *
     * @param classToCheck the class to check
     * @param properties   the exception properties
     */
    public static void checkIfInjectable(Class<?> classToCheck, String... properties) {
        try {
            injector.getBinding(classToCheck);
        } catch (ConfigurationException e) {
            throwSeedException(CoreUtilsErrorCode.INJECTABLE_CHECK_FAILED, e, properties);
        }
    }

    /**
     * Checks if the specified instance satisfy a specification. If not, throws a SeedException with SATISFIED_BY_CHECK_FAILED
     * error code.
     *
     * @param actual        the instance to check
     * @param specification the specified to test for
     * @param properties    the exception properties
     * @param <T>           the type of the instance
     */
    public static <T> void checkIfSatisfiedBy(T actual, Specification<T> specification, String... properties) {
        if (!specification.isSatisfiedBy(actual)) {
            throwSeedException(CoreUtilsErrorCode.SATISFIED_BY_CHECK_FAILED, null, properties);
        }
    }

    @SuppressWarnings("unchecked")
    private static void throwSeedException(ErrorCode errorCode, Throwable cause, String... properties) {
        SeedException seedException;

        if (cause != null) {
            seedException = SeedException.wrap(cause, errorCode);
        } else {
            seedException = SeedException.createNew(errorCode);
        }

        for (int i = 0; i < properties.length; i = i + 2) {
            String key = properties[i];
            String value = properties[i + 1];
            seedException.put(key, value);
        }

        throw seedException;
    }
}
