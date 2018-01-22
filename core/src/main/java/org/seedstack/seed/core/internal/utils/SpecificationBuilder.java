/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.utils;

import java.util.function.Predicate;
import org.kametic.specifications.AbstractSpecification;
import org.kametic.specifications.Specification;

public class SpecificationBuilder<T> {
    private Predicate<T> predicate;

    public SpecificationBuilder(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    public SpecificationBuilder and(Predicate<? super T>... others) {
        for (Predicate<? super T> other : others) {
            predicate = predicate.and(other);
        }
        return this;
    }

    public SpecificationBuilder or(Predicate<? super T>... others) {
        for (Predicate<? super T> other : others) {
            predicate = predicate.or(other);
        }
        return this;
    }

    public SpecificationBuilder negate() {
        predicate = predicate.negate();
        return this;
    }

    public Specification<T> build() {
        return new AbstractSpecification<T>() {
            @Override
            public boolean isSatisfiedBy(T candidate) {
                return predicate.test(candidate);
            }
        };
    }
}
