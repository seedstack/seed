/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.utils;

import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import org.seedstack.shed.reflect.ExecutablePredicates;

import java.lang.reflect.Method;
import java.util.function.Predicate;

public class MethodMatcherBuilder {
    private Predicate<Method> predicate;

    public MethodMatcherBuilder(Predicate<Method> predicate) {
        this.predicate = predicate;
    }

    public MethodMatcherBuilder and(Predicate<? super Method>... others) {
        for (Predicate<? super Method> other : others) {
            predicate = predicate.and(other);
        }
        return this;
    }

    public MethodMatcherBuilder or(Predicate<? super Method>... others) {
        for (Predicate<? super Method> other : others) {
            predicate = predicate.or(other);
        }
        return this;
    }

    public MethodMatcherBuilder negate() {
        predicate = predicate.negate();
        return this;
    }

    public Matcher<Method> build() {
        return new PredicateMatcherAdapter(ExecutablePredicates.<Method>executableIsSynthetic().negate().and(predicate));
    }

    private static class PredicateMatcherAdapter extends AbstractMatcher<Method> {
        private final Predicate<Method> effectivePredicate;

        private PredicateMatcherAdapter(Predicate<Method> effectivePredicate) {
            this.effectivePredicate = effectivePredicate;
        }

        @Override
        public boolean matches(Method executable) {
            return effectivePredicate.test(executable);
        }
    }
}
