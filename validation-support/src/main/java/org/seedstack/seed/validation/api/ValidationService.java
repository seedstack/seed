/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.validation.api;

import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * Interface for validation service.
 *
 * @author epo.jemba@ext.mpsa.com
 */
public interface ValidationService {
    String JAVAX_VALIDATION_CONSTRAINT_VIOLATIONS = "Set<javax.validation.ConstraintViolation>";

    /**
     * Validate a class statically.
     *
     * @param <T>       the type of the class to validate.
     * @param candidate the class to validate.
     */
    <T> void staticallyHandle(T candidate);

    /**
     * Dynamically validate an intercepted method invocation.
     *
     * @param invocation the {@link org.aopalliance.intercept.MethodInvocation} to execute.
     * @return the return value of the invocation.
     * @throws Throwable if anything goes wrong during the invocation.
     */
    Object dynamicallyHandleAndProceed(MethodInvocation invocation) throws Throwable;

    /**
     * Checks if the class can be statically validated.
     *
     * @param candidate the class to assess.
     * @return true if it can be statically validated, false otherwise.
     */
    boolean candidateForStaticValidation(Class<?> candidate);

    /**
     * Checks if a method can be dynamically validated.
     *
     * @param candidate the method to assess.
     * @return true if it can be dynamically validated, false otherwise.
     */
    boolean candidateForDynamicValidation(Method candidate);

    /**
     * Checks if a class can be dynamically validated.
     *
     * @param candidate the class to assess.
     * @return true if it can be dynamically validated, false otherwise.
     */
    boolean candidateForDynamicValidation(Class<?> candidate);

}