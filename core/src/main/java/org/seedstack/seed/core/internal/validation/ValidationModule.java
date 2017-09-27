/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.validation;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Provides;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.validation.Constraint;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import org.seedstack.shed.reflect.Annotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ValidationConcern
class ValidationModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationModule.class);
    private final ValidatorFactory validatorFactory;

    ValidationModule(ValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
    }

    @Override
    protected void configure() {
        bind(ValidatorFactory.class).toInstance(validatorFactory);
        enableValidationOnInjectionPoints();
        if (isDynamicValidationSupported()) {
            configureDynamicValidation();
        }
    }

    private void enableValidationOnInjectionPoints() {
        bindListener(staticValidationMatcher(), new StaticValidationProvisionListener(validatorFactory));
    }

    private void configureDynamicValidation() {
        bindInterceptor(Matchers.any(), dynamicValidationMatcher(), new MethodValidationInterceptor(validatorFactory));
    }

    private boolean isDynamicValidationSupported() {
        ExecutableValidator executableValidator = null;
        try {
            executableValidator = validatorFactory.getValidator().forExecutables();
        } catch (Throwable t) {
            LOGGER.warn("Unable to create the dynamic validator, support for dynamic validation disabled", t);
        }
        return executableValidator != null;
    }

    @Provides
    Validator provideValidator() {
        return validatorFactory.getValidator();
    }

    private Matcher<? super Binding<?>> staticValidationMatcher() {
        return new AbstractMatcher<Binding<?>>() {
            @Override
            public boolean matches(Binding<?> binding) {
                Class<?> candidate = binding.getKey().getTypeLiteral().getRawType();
                for (Field field : candidate.getDeclaredFields()) {
                    for (Annotation annotation : field.getAnnotations()) {
                        if (hasConstraintOrValidAnnotation(annotation)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    private Matcher<Method> dynamicValidationMatcher() {
        return new AbstractMatcher<Method>() {
            @Override
            public boolean matches(Method method) {
                return shouldValidateParameters(method) || shouldValidateReturnType(method);
            }
        };
    }

    private boolean shouldValidateParameters(Method candidate) {
        for (Annotation[] annotationsForOneParameter : candidate.getParameterAnnotations()) {
            for (Annotation annotation : annotationsForOneParameter) {
                if (hasConstraintOrValidAnnotation(annotation)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldValidateReturnType(Method candidate) {
        for (Annotation annotation : candidate.getAnnotations()) {
            if (hasConstraintOrValidAnnotation(annotation)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasConstraintOrValidAnnotation(Annotation annotation) {
        return Annotations.on(annotation.annotationType()).includingMetaAnnotations().find(
                Constraint.class).isPresent() ||
                Valid.class.equals(annotation.annotationType());
    }
}
