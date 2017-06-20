/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
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

import javax.inject.Singleton;
import java.lang.annotation.Annotation;

class AutoCloseableMatcher extends AbstractMatcher<Binding<?>> {
    private final AutoCloseableScopingVisitor autoCloseableScopingVisitor = new AutoCloseableScopingVisitor();

    @Override
    public boolean matches(Binding<?> binding) {
        return AutoCloseable.class.isAssignableFrom(binding.getKey().getTypeLiteral().getRawType()) && binding.acceptScopingVisitor(autoCloseableScopingVisitor);
    }

    private static class AutoCloseableScopingVisitor implements BindingScopingVisitor<Boolean> {
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
            return scopeAnnotation.isAssignableFrom(Singleton.class) || scopeAnnotation.isAssignableFrom(com.google.inject.Singleton.class);
        }

        @Override
        public Boolean visitNoScoping() {
            return false;
        }
    }
}
