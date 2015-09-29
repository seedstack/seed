/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.el.internal;

import com.google.inject.Binder;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import org.seedstack.seed.core.utils.SeedReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * ELBinder provides methods to bind an {@link ELInterceptor} on a given annotation.
 * <p>
 * The evaluation of the EL provided by the annotation value will be evaluated regarding the
 * {@link ELBinder.ExecutionPolicy}, ie. before the method is proceed, after, or both.
 * </p>
 * Example:
 * <pre>
 *     public class MyModule extends AbstractModule {
 *
 *         {@literal @}Override
 *         protected void configure() {
 *             // the EL will be evaluated before the method proceed
 *             new ELBinder(this.binder())
 *                 .bindELAnnotation(MyELAnnotation.class, ELBinder.ExecutionPolicy.BEFORE);
 *         }
 *     }
 * </pre>
 * <p>
 * By default, if no policy is given the EL will be evaluated after the method proceed.
 * </p>
 *
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 01/07/2014
 */
public class ELBinder {

    private final Binder binder;

    /**
     * ExecutionPolicy define when an EL will be executed. Before, after or before and after the annotated
     * method proceed.
     */
    public enum ExecutionPolicy {
        BEFORE,
        AFTER,
        BOTH
    }

    /**
     * Constructor.
     *
     * @param binder the binder of the current module
     */
    public ELBinder(Binder binder) {
        this.binder = binder;
    }

    /**
     * Bind an ELInterceptor to the given annotation with the default
     * {@link ELBinder.ExecutionPolicy} AFTER.
     *
     * @param annotationClass EL annotation to intercept
     * @return ELBinder
     */
    public ELBinder bindELAnnotation(Class<? extends Annotation> annotationClass) {
        return bindELAnnotation(annotationClass, ExecutionPolicy.AFTER);
    }

    /**
     * Bind an ELInterceptor to the given annotation with the given
     * {@link ELBinder.ExecutionPolicy}.
     *
     * @param annotationClass EL annotation to intercept
     * @param policy          determine when the EL is evaluated
     * @return ELBinder
     */
    public ELBinder bindELAnnotation(Class<? extends Annotation> annotationClass, ExecutionPolicy policy) {
        ELInterceptor interceptor = new ELInterceptor(annotationClass, policy);
        binder.requestInjection(interceptor);
        binder.bindInterceptor(Matchers.any(), handlerMethod(annotationClass), interceptor);
        return this;
    }

    private Matcher<Method> handlerMethod(final Class<? extends Annotation> annotationClass) {
        return new AbstractMatcher<Method>() {

            @Override
            public boolean matches(Method candidate) {
                return !candidate.isSynthetic() && SeedReflectionUtils.getMetaAnnotationFromAncestors(candidate, annotationClass) != null;
            }

        };
    }
}
