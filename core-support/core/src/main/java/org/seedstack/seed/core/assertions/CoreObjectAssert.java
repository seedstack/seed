/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.assertions;

import com.google.common.base.Objects;
import org.seedstack.seed.core.api.SeedException;
import org.assertj.core.api.AbstractObjectAssert;
import org.seedstack.seed.core.utils.SeedReflectionUtils;

import java.lang.reflect.Method;

/**
 * This assertion class provides assertions around instances.
 *
 * @param <A> the type of the asserted object.
 * @author epo.jemba@ext.mpsa.com
 */
public class CoreObjectAssert<A> extends AbstractObjectAssert<CoreObjectAssert<A>, A> {

    /**
     * Creates the assertion with the specified instance.
     *
     * @param actual the instance to test
     */
    public CoreObjectAssert(A actual) {
        super(actual, CoreObjectAssert.class);
    }

    /**
     * Check if a property exists and if it equals to expectedValue.
     *
     * @param propertyName  the property name to check for
     * @param expectedValue the expected value
     * @return itself
     */
    public CoreObjectAssert<A> propertyEquals(String propertyName, Object expectedValue) {
        Method method;
        Object returnedObject;
        String computedMethodName = null;
        Class<?> actualClass = SeedReflectionUtils.cleanProxy(actual.getClass());

        // reach method
        try {
            computedMethodName = "get" + propertyName.substring(0, 1).toUpperCase();
            if (computedMethodName.length() > 1) {
                computedMethodName += propertyName.substring(1);
            }

            method = actualClass.getMethod(computedMethodName);
        } catch (Exception e) {
            throw SeedException.wrap(e, CoreAssertionsErrorCode.UNEXPECTED_EXCEPTION)
                    .put("more", String.format("The method [%s] does not exists in class [%s]", computedMethodName, actualClass));
        }

        // call the method
        try {
            returnedObject = method.invoke(actual);
        } catch (Exception e) {
            throw SeedException.wrap(e, CoreAssertionsErrorCode.UNEXPECTED_EXCEPTION)
                    .put("more", String.format("The method [%s] on object [%s] can not be executed.", computedMethodName, actual));
        }

        SeedException.createNew(CoreAssertionsErrorCode.BAD_PROPERTY_VALUE)
                .put("property", propertyName)
                .put("expectedValue", expectedValue)
                .put("actualValue", returnedObject)
                .throwsIf(!Objects.equal(expectedValue, returnedObject));

        return myself;
    }

    @Override
    public CoreObjectAssert<A> isNotNull() {
        SeedException.createNew(CoreAssertionsErrorCode.OBJECT_IS_NULL).throwsIfNull(actual);
        return myself;
    }

}
