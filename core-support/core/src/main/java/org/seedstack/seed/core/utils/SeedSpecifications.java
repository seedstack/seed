/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

import org.kametic.specifications.AbstractSpecification;
import org.kametic.specifications.Specification;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

/**
 * Class with various utility Nuun specifications.
 *
 * @author adrien.lauer@mpsa.com
 */
public final class SeedSpecifications {

    private SeedSpecifications() {
    }

    /**
     * Specify classes meta annotated with an annotation.
     * @param klass the class of the annotation.
     * @return the specification.
     */
    public static Specification<Class<?>> classMetaAnnotatedWith(final Class<? extends Annotation> klass) {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {
                return candidate != null && SeedReflectionUtils.hasAnnotationDeep(candidate, klass);
            }
        };
    }

    /**
     * Specify classes which are interfaces.
     * @return the specification.
     */
    public static Specification<Class<?>> classIsInterface() {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {
                return candidate != null && candidate.isInterface();
            }
        };
    }

    /**
     * Specify abstract classes.
     * @return the specification.
     */
    public static Specification<Class<?>> classIsAbstract() {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {
                return candidate != null && Modifier.isAbstract(candidate.getModifiers());
            }
        };
    }
}
