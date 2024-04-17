/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.lifecycle;

import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.spi.BindingScopingVisitor;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;

final class SingletonScopingVisitor implements BindingScopingVisitor<Boolean> {
    static final SingletonScopingVisitor INSTANCE = new SingletonScopingVisitor();

    private SingletonScopingVisitor() {
    }

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
