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

import com.google.inject.Injector;
import org.seedstack.seed.core.api.SeedException;
import org.assertj.core.api.AbstractClassAssert;
import org.seedstack.seed.core.utils.SeedReflectionUtils;

import javax.inject.Inject;
import java.lang.reflect.Method;

/**
 * This assertion class provides assertions around classes.
 *
 * @param <S> the assertion type.
 * @author epo.jemba@ext.mpsa.com
 */
public class CoreClassAssert<S extends CoreClassAssert<S>> extends AbstractClassAssert<S> {
    @Inject
    private static Injector injector;

    /**
     * Creates the assertion with the specified types.
     *
     * @param actual   the class to test
     * @param selfType the self type of this assertion class ({@link AbstractClassAssert}
     */
    protected CoreClassAssert(Class<?> actual, Class<?> selfType) {
        super(actual, selfType);
    }

    /**
     * Checks if the class is injectable.
     *
     * @return itself
     */
    public S isInjectable() {
        try {
            injector.getInstance(actual);
        } catch (Exception e) {
            throw SeedException
                    .wrap(e, CoreAssertionsErrorCode.CLASS_IS_NOT_INJECTABLE)
                    .put("className", actual.getName())
                    .put("more", "\n" + e.getMessage());
        }

        return myself;
    }

    /**
     * Checks if the class is injected with an instance assignable to the specified class.
     *
     * @param candidate the class to test for assignation
     * @return itself
     */
    public S isInjectedWithInstanceOf(Class<?> candidate) {
        Object injectee;

        try {
            injectee = injector.getInstance(actual);
        } catch (Exception e) {
            throw SeedException
                    .wrap(e, CoreAssertionsErrorCode.CLASS_IS_NOT_INJECTABLE)
                    .put("className", actual.getName())
                    .put("more", "\n" + e.getMessage());
        }

        SeedException
                .createNew(CoreAssertionsErrorCode.CLASS_IS_NOT_INJECTED_WITH)
                .put("className", actual.getName())
                .put("injectee", candidate.getName())
                .put("actual", injectee.getClass().getName())
                .throwsIf(!injectee.getClass().equals(candidate) && !candidate.isAssignableFrom(injectee.getClass()));

        return myself;
    }

    /**
     * Checks if the class has the specified method.
     *
     * @param expectedMethodName the method name to check for.
     * @return itself.
     */
    public S hasDeclaredMethod(String expectedMethodName) {
        Class<?> actualClass = SeedReflectionUtils.cleanProxy(actual);
        boolean found = false;

        try {
            for (Method method : actualClass.getDeclaredMethods()) {
                if (found = method.getName().equals(expectedMethodName)) {
                    break;
                }
            }
        } catch (Exception e) {
            throw SeedException.wrap(e, CoreAssertionsErrorCode.METHOD_DOES_NOT_EXIST)
                    .put("methodName", expectedMethodName)
                    .put("className", actualClass.getName());
        }

        SeedException.createNew(CoreAssertionsErrorCode.METHOD_DOES_NOT_EXIST)
                .put("methodName", expectedMethodName)
                .put("className", actualClass.getName())
                .throwsIf(!found);

        return myself;
    }
}
