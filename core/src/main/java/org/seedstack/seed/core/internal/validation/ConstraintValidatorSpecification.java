/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.validation;

import java.lang.reflect.Modifier;
import javax.validation.ConstraintValidator;
import org.kametic.specifications.AbstractSpecification;
import org.seedstack.shed.reflect.ClassPredicates;

class ConstraintValidatorSpecification extends AbstractSpecification<Class<?>> {
    static ConstraintValidatorSpecification INSTANCE = new ConstraintValidatorSpecification();

    private ConstraintValidatorSpecification() {
        // no instantiation allowed
    }

    @Override
    public boolean isSatisfiedBy(Class<?> candidate) {
        return ClassPredicates.classIsAssignableFrom(ConstraintValidator.class)
                .and(ClassPredicates.classIsInterface().negate())
                .and(ClassPredicates.classModifierIs(Modifier.ABSTRACT).negate())
                .test(candidate);
    }
}
