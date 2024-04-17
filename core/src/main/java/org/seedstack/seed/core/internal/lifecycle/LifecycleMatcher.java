/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.lifecycle;

import com.google.inject.Binding;
import com.google.inject.matcher.AbstractMatcher;
import org.seedstack.shed.reflect.Classes;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static org.seedstack.shed.reflect.AnnotationPredicates.elementAnnotatedWith;

class LifecycleMatcher extends AbstractMatcher<Binding<?>> {
    @Override
    public boolean matches(Binding<?> binding) {
        // Singletons being auto-closeable or having at least one @PreDestroy method match
        // Any binding (any scope) having at least one @PostConstruct method match
        Class<?> rawType = binding.getKey().getTypeLiteral().getRawType();
        return (binding.acceptScopingVisitor(SingletonScopingVisitor.INSTANCE) && (isAutoCloseable(rawType) || hasPreDestroy(rawType))) || hasPostConstruct(rawType);
    }

    private boolean hasPreDestroy(Class<?> rawType) {
        return Classes.from(rawType)
                .traversingInterfaces()
                .traversingSuperclasses()
                .methods()
                .anyMatch(elementAnnotatedWith(PreDestroy.class, true));
    }

    private boolean hasPostConstruct(Class<?> rawType) {
        return Classes.from(rawType)
                .traversingInterfaces()
                .traversingSuperclasses()
                .methods()
                .anyMatch(elementAnnotatedWith(PostConstruct.class, true));
    }

    private boolean isAutoCloseable(Class<?> rawType) {
        return AutoCloseable.class.isAssignableFrom(rawType);
    }
}
