/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.util.Types;
import org.seedstack.seed.core.utils.SeedCheckUtils;
import org.seedstack.seed.spi.dependency.DependencyProvider;
import org.seedstack.seed.spi.dependency.Maybe;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

class CoreModule extends AbstractModule {
    private final Collection<Module> subModules;
    private final Map<Class<?>, Maybe<? extends DependencyProvider>> optionalDependencies;

    CoreModule(Collection<Module> subModules, Map<Class<?>, Maybe<? extends DependencyProvider>> optionalDependencies) {
        this.subModules = subModules;
        this.optionalDependencies = optionalDependencies;
    }

    @Override
    protected void configure() {
        // Static utils
        requestStaticInjection(SeedCheckUtils.class);

        // Logging
        bindListener(Matchers.any(), new LoggingTypeListener());

        // Install detected modules
        subModules.forEach(this::install);

        // Optional dependencies
        for (final Entry<Class<?>, Maybe<? extends DependencyProvider>> dependency : this.optionalDependencies.entrySet()) {
            @SuppressWarnings("unchecked")
            TypeLiteral<Maybe<? extends DependencyProvider>> typeLiteral = (TypeLiteral<Maybe<? extends DependencyProvider>>) TypeLiteral.get(Types.newParameterizedType(Maybe.class, dependency.getKey()));
            bind(typeLiteral).toInstance(dependency.getValue());
        }
    }
}
