/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.lifecycle;

import com.google.inject.Binding;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.BindingScopingVisitor;
import org.seedstack.shed.reflect.Classes;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;

import static org.seedstack.shed.reflect.AnnotationPredicates.elementAnnotatedWith;

class DestructionMatcher extends AbstractMatcher<Binding<?>> {
    private final SingletonScopingVisitor singletonScopingVisitor = new SingletonScopingVisitor();

    @Override
    public boolean matches(Binding<?> binding) {
        Class<?> rawType = binding.getKey().getTypeLiteral().getRawType();
        return binding.acceptScopingVisitor(singletonScopingVisitor) &&
                (isAutoCloseable(rawType) || hasPreDestroy(rawType));
    }

    private boolean hasPreDestroy(Class<?> rawType) {
        return Classes.from(rawType)
                .traversingInterfaces()
                .traversingSuperclasses()
                .methods()
                .anyMatch(elementAnnotatedWith(PreDestroy.class, true));
    }

    private boolean isAutoCloseable(Class<?> rawType) {
        return AutoCloseable.class.isAssignableFrom(rawType);
    }

    private static class SingletonScopingVisitor implements BindingScopingVisitor<Boolean> {
        @Override
        public Boolean visitEagerSingleton() {
            return true;
        }

        @Override
        public Boolean visitScope(Scope scope) {
            return scope == Scopes.SINGLETON;
        }

        @Override
        public Boolean visitScopeAnnotation(Class<? extends Annotation> scopeAnnotation) {
            return scopeAnnotation.isAssignableFrom(Singleton.class) || scopeAnnotation.isAssignableFrom(
                    com.google.inject.Singleton.class);
        }

        @Override
        public Boolean visitNoScoping() {
            return false;
        }
    }
}
