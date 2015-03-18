/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.validation.internal;


import org.seedstack.seed.validation.api.ValidationException;
import org.seedstack.seed.validation.api.ValidationService;
import org.aopalliance.intercept.MethodInvocation;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.Constraint;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;


/**
 * Internal validation service. It handles static validation, and by contract validation thanks to Validator and ExecutableValidator.
 *
 * @author epo.jemba@ext.mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 */
class ValidationServiceInternal implements ValidationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationServiceInternal.class);

    @Inject
    private Validator validator;

    @Inject @Nullable
    private ExecutableValidator executableValidator;

    @Override
    public <T> void staticallyHandle(T candidate) {

        Set<ConstraintViolation<T>> constraintViolations = validator.validate(candidate);

        if (!constraintViolations.isEmpty()) {

            ValidationException newException = SeedException.createNew(ValidationException.class, ValidationErrorCode.VALIDATION_ISSUE);
            StringBuffer exceptionMessage = new StringBuffer();
            exceptionMessage.append("Constraint violations on ");
            int i = 1;
            boolean first = true;

            for (ConstraintViolation<T> violation : constraintViolations) {

                LOGGER.debug("<violation {} > ", i);
                LOGGER.debug("{} : {}", violation.getMessage(), violation.getInvalidValue());
                Class<?> rootBeanClass = SeedReflectionUtils.cleanProxy(violation.getRootBeanClass());
                LOGGER.debug("Path : {}.{}", rootBeanClass.getCanonicalName(), violation.getPropertyPath());
                LOGGER.debug("</violation> ");

                if (first) {
                    exceptionMessage.append(rootBeanClass.getName()).append("\n");
                    first = false;
                }
                exceptionMessage.append("\t").append(violation.getPropertyPath()).append(" - ").append(violation.getMessage()).append(", but ").append(violation.getInvalidValue()).append(" was found.\n");

                ++i;
            }


            newException.put("message", exceptionMessage);
            newException.put(JAVAX_VALIDATION_CONSTRAINT_VIOLATIONS, constraintViolations);
            throw newException;
        }
    }



    @Override
    public Object dynamicallyHandleAndProceed(MethodInvocation invocation) throws Throwable {
        if (executableValidator == null) {
            throw SeedException.createNew(ValidationErrorCode.DYNAMIC_VALIDATION_IS_NOT_SUPPORTED);
        }

        // TODO : ajout des groupes
        Object this1 = invocation.getThis();
        Method method = invocation.getMethod();
        Object[] arguments = invocation.getArguments();
        Set<ConstraintViolation<Object>> parametersConstraintViolations = executableValidator.validateParameters(this1, method, arguments /*, groups */);

        handleConstraintViolations(parametersConstraintViolations);

        Object returnValue = invocation.proceed();

        Set<ConstraintViolation<Object>> returnValueConstraintViolations = executableValidator.validateReturnValue(this1, method, returnValue /*, groups*/);

        handleConstraintViolations(returnValueConstraintViolations);

        return returnValue;
    }


    private void handleConstraintViolations(Set<ConstraintViolation<Object>> constraintViolations) {
        if (!constraintViolations.isEmpty()) {
            ValidationException newException = SeedException.createNew(ValidationException.class, ValidationErrorCode.VALIDATION_ISSUE);

            int i = 0;
            for (ConstraintViolation<Object> violation : constraintViolations) {

                Class<?> rootBeanClass = SeedReflectionUtils.cleanProxy(violation.getRootBeanClass());

                LOGGER.debug("<violation {} > ", i);
                LOGGER.debug("{} : {}", violation.getMessage(), violation.getInvalidValue());
                LOGGER.debug("Path : {}.{}", rootBeanClass.getCanonicalName(), violation.getPropertyPath());
                LOGGER.debug("</violation> ");

                newException.put(String.format("%d - %s", i, violation.getMessage()), violation.getInvalidValue());
                newException.put(String.format("%d - Path", i), rootBeanClass + "." + violation.getPropertyPath());
                ++i;
            }

            throw newException;
        }
    }

    @Override
    public boolean candidateForStaticValidation(Class<?> candidate) {
        boolean isCandidate = false;

        // look for fields
        for (Field field : candidate.getDeclaredFields()) {
            for (Annotation annotation : field.getAnnotations()) {
                if (SeedReflectionUtils.hasAnnotationDeep(annotation.annotationType(), Constraint.class) || Valid.class.equals(annotation.annotationType())) {
                    // check if the annotation type is itself annotated with
                    // Constraint making it a constraint
                    isCandidate = true;
                    break;
                }
            }
            if (isCandidate) {
                break;
            }
        }

        return isCandidate;
    }

    @Override
    public boolean candidateForDynamicValidation(Method candidate) {
        boolean isCandidate = false;
        // Check Parameters
        for (Annotation[] annotationsForOneParameter : candidate.getParameterAnnotations()) {
            for (Annotation annotation : annotationsForOneParameter) {
                if (SeedReflectionUtils.hasAnnotationDeep(annotation.annotationType(), Constraint.class) || Valid.class.equals(annotation.annotationType())) {
                    // check if the annotation type is itself annotated with Constraint making it a constraint
                    isCandidate = true;
                    break;
                }
            }
            if (isCandidate) {
                break;
            }
        }

        // Check returned type
        if (!isCandidate) {
            for (Annotation annotation : candidate.getAnnotations()) {
                if (SeedReflectionUtils.hasAnnotationDeep(annotation.annotationType(), Constraint.class) || Valid.class.equals(annotation.annotationType())) {
                    isCandidate = true;
                }
            }
        }

        return isCandidate;

    }

    @Override
    public boolean candidateForDynamicValidation(Class<?> candidate) {
        // look for methods

        for (Method method : candidate.getDeclaredMethods()) {
            if (candidateForDynamicValidation(method)) {
                return true;
            }
        }

        return false;
    }


}
