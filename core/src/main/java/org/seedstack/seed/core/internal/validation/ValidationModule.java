/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.validation;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.seedstack.seed.core.internal.init.ValidationManager;
import org.seedstack.shed.reflect.Annotations;

@ValidationConcern
class ValidationModule extends AbstractModule {
    private final ValidationManager.ValidationLevel level;
    private final Set<Class<? extends ConstraintValidator>> constraintValidators;

    ValidationModule(ValidationManager.ValidationLevel level,
            Set<Class<? extends ConstraintValidator>> constraintValidators) {
        this.level = level;
        this.constraintValidators = constraintValidators;
    }

    @Override
    protected void configure() {
        install(new PrivateModule() {
            @Override
            protected void configure() {
                // Validator factory
                bind(ValidatorFactory.class).toProvider(ValidatorFactoryProvider.class).in(Scopes.SINGLETON);
                expose(ValidatorFactory.class);

                // Validator
                bind(Validator.class).toProvider(ValidatorProvider.class).in(Scopes.SINGLETON);
                expose(Validator.class);

                // Detected constraint validators
                constraintValidators.forEach(this::bind);
            }
        });

        // Validation on injection / method call
        enableValidationOnInjectionPoints();
        if (isDynamicValidationSupported()) {
            configureDynamicValidation();
        }
    }

    private void enableValidationOnInjectionPoints() {
        StaticValidationProvisionListener staticValidationProvisionListener = new StaticValidationProvisionListener();
        requestInjection(staticValidationProvisionListener);
        bindListener(staticValidationMatcher(), staticValidationProvisionListener);
    }

    private void configureDynamicValidation() {
        MethodValidationInterceptor methodValidationInterceptor = new MethodValidationInterceptor();
        requestInjection(methodValidationInterceptor);
        bindInterceptor(Matchers.any(), dynamicValidationMatcher(), methodValidationInterceptor);
    }

    private boolean isDynamicValidationSupported() {
        return level.compareTo(ValidationManager.ValidationLevel.LEVEL_1_1) >= 0;
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
