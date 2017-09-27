/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.validation;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.seedstack.seed.SeedException;

class MethodValidationInterceptor implements MethodInterceptor {
    private final ValidatorFactory validatorFactory;

    MethodValidationInterceptor(ValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        ExecutableValidator executableValidator = validatorFactory.getValidator().forExecutables();
        if (executableValidator == null) {
            throw SeedException.createNew(ValidationErrorCode.DYNAMIC_VALIDATION_IS_NOT_SUPPORTED);
        }

        validateParameters(invocation, executableValidator);
        Object returnValue = invocation.proceed();
        validateReturnValue(invocation, executableValidator, returnValue);
        return returnValue;
    }

    private void validateParameters(MethodInvocation invocation, ExecutableValidator executableValidator) {
        // validation by interception is always done with the default group
        Set<ConstraintViolation<Object>> constraintViolations = executableValidator.validateParameters(
                invocation.getThis(),
                invocation.getMethod(),
                invocation.getArguments()
        );
        if (!constraintViolations.isEmpty()) {
            throw new VerboseConstraintViolationException(constraintViolations);
        }
    }

    private void validateReturnValue(MethodInvocation invocation, ExecutableValidator executableValidator,
            Object returnValue) {
        Set<ConstraintViolation<Object>> constraintViolations = executableValidator.validateReturnValue(
                invocation.getThis(),
                invocation.getMethod(),
                returnValue
                /*, groups*/
        );
        if (!constraintViolations.isEmpty()) {
            throw new VerboseConstraintViolationException(constraintViolations);
        }
    }
}
