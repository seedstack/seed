/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.dependency;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.seedstack.seed.spi.DependencyProvider;

class DependencyModule extends AbstractModule {
    private final Map<Class<?>, Optional<? extends DependencyProvider>> dependencies;

    DependencyModule(Map<Class<?>, Optional<? extends DependencyProvider>> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    protected void configure() {
        for (final Entry<Class<?>, Optional<? extends DependencyProvider>> dependency : this.dependencies.entrySet()) {
            @SuppressWarnings("unchecked")
            TypeLiteral<Optional<? extends DependencyProvider>> typeLiteral = (TypeLiteral<Optional<? extends
                    DependencyProvider>>) TypeLiteral.get(
                    Types.newParameterizedType(Optional.class, dependency.getKey()));
            bind(typeLiteral).toInstance(dependency.getValue());
        }
    }
}
