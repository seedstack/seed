/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.seedstack.seed.SeedInterceptor;

class CoreModule extends AbstractModule {
    private final Collection<? extends Module> modules;
    private final Set<Bindable<?>> bindings;
    private final List<? extends SeedInterceptor> interceptors;

    CoreModule(Collection<? extends Module> modules, Set<Bindable<?>> bindings,
            List<? extends SeedInterceptor> interceptors) {
        this.modules = modules;
        this.bindings = bindings;
        this.interceptors = interceptors;
    }

    @Override
    protected void configure() {
        modules.forEach(this::install);
        bindings.forEach(binding -> binding.apply(binder()));
        interceptors.forEach(interceptor -> {
            bindInterceptor(createMatcherFromPredicate(interceptor.classPredicate()),
                    createMatcherFromPredicate(interceptor.methodPredicate()),
                    interceptor);
            requestInjection(interceptor);
        });
    }

    private <T> Matcher<T> createMatcherFromPredicate(Predicate<T> predicate) {
        return new AbstractMatcher<T>() {
            @Override
            public boolean matches(T t) {
                return predicate.test(t);
            }
        };
    }
}
