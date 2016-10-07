/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.Test;
import org.seedstack.seed.spi.dependency.DependencyProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CoreModuleTest {
    @Test
    public void testConfigure(@Mocked final Binder binder, @Mocked final DependencyProvider myProvider) {
        Collection<Module> subModules = new ArrayList<>();

        Map<Class<?>, Optional<? extends DependencyProvider>> optionalDependencies = new HashMap<>();
        final Optional<DependencyProvider> maybe = Optional.of(myProvider);
        optionalDependencies.put(DependencyProvider.class, maybe);

        CoreModule module = new CoreModule(subModules, optionalDependencies);
        module.configure(binder);

        new Verifications() {
            {
                binder.bind(new TypeLiteral<Optional<DependencyProvider>>() {
                }).toInstance(maybe);
            }
        };
    }
}
