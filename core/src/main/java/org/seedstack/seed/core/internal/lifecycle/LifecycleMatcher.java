/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.lifecycle;

import static org.seedstack.shed.reflect.AnnotationPredicates.elementAnnotatedWith;

import com.google.inject.Binding;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.BindingScopingVisitor;
import java.lang.annotation.Annotation;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import org.seedstack.shed.reflect.Classes;

class LifecycleMatcher extends AbstractMatcher<Binding<?>> {
    private final PreDestroyScopingVisitor preDestroyScopingVisitor = new PreDestroyScopingVisitor();

    @Override
    public boolean matches(Binding<?> binding) {
        Class<?> rawType = binding.getKey().getTypeLiteral().getRawType();
        return binding.acceptScopingVisitor(preDestroyScopingVisitor) &&
                (isAutoCloseable(rawType) || hasJsr250Methods(rawType));
    }

    private boolean hasJsr250Methods(Class<?> rawType) {
        return Classes.from(rawType)
                .traversingInterfaces()
                .traversingSuperclasses()
                .methods()
                .anyMatch(elementAnnotatedWith(PreDestroy.class, true)
                        .or(elementAnnotatedWith(PostConstruct.class, true)));
    }

    private boolean isAutoCloseable(Class<?> rawType) {
        return AutoCloseable.class.isAssignableFrom(rawType);
    }

    private static class PreDestroyScopingVisitor implements BindingScopingVisitor<Boolean> {
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
