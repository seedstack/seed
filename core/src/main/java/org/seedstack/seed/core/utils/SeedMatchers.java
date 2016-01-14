/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Class with various utility Guice matchers.
 *
 * @author adrien.lauer@mpsa.com
 */
public final class SeedMatchers {

    private SeedMatchers() {
    }

    /**
     * Guice matcher for classes which ancestor classes are meta annotated with a specified annotation.
     *
     * @param anoKlass the class of the annotation.
     * @return the Guice matcher.
     */
    public static Matcher<Class<?>> ancestorMetaAnnotatedWith(final Class<? extends Annotation> anoKlass) {
        return new AbstractMatcher<Class<?>>() {

            @Override
            public boolean matches(Class<?> candidate) {
                if (candidate == null) {
                    return false;
                }

                boolean result = false;

                Class<?>[] allInterfacesAndClasses = SeedReflectionUtils.getAllInterfacesAndClasses(candidate);

                for (Class<?> clazz : allInterfacesAndClasses) {
                    if (SeedSpecifications.classMetaAnnotatedWith(anoKlass).isSatisfiedBy(clazz)) {
                        result = true;
                        break;
                    }
                }

                return result;
            }
        };
    }

    /**
     * Guice matcher for methods or classes which ancestor classes are meta annotated with a specified annotation.
     *
     * @param anoKlass the annotation class.
     * @return the Guice matcher.
     */
    public static Matcher<Method> methodOrAncestorMetaAnnotatedWith(final Class<? extends Annotation> anoKlass) {
        return new AbstractMatcher<Method>() {
            @Override
            public boolean matches(Method method) {
                return SeedReflectionUtils.getMethodOrAncestorMetaAnnotatedWith(method, anoKlass) != null;
            }
        };
    }

    /**
     * Guice matcher for non-synthetic methods.
     *
     * @return the Guice matcher.
     */
    public static Matcher<Method> methodNotSynthetic() {
        return new AbstractMatcher<Method>() {
            @Override
            public boolean matches(Method method) {
                return !method.isSynthetic();
            }
        };
    }

    /**
     * Guice matcher for methods which are NOT defined in Object.
     *
     * @return the Guice matcher.
     */
    public static Matcher<Method> methodNotOfObject() {
        return new AbstractMatcher<Method>() {
            @Override
            public boolean matches(Method method) {
                return !method.getDeclaringClass().equals(Object.class);
            }
        };
    }
}
