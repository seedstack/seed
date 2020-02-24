/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

import java.lang.reflect.Method;
import java.util.function.Predicate;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * Any class implementing this interface will be detected and registered as a method interceptor that can be potentially
 * applied to all object instances managed by SeedStack.
 */
public interface SeedInterceptor extends MethodInterceptor {
    /**
     * The class predicate which determines classes this interceptor should be applied to.
     *
     * @return the class predicate.
     */
    Predicate<Class<?>> classPredicate();

    /**
     * The method predicate which determines method this interceptor should be applied to. Only method of classes
     * matching the {@link #classPredicate()} are considered.
     *
     * @return the class predicate.
     */
    Predicate<Method> methodPredicate();
}
