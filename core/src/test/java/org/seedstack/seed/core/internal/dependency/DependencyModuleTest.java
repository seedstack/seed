/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.dependency;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.Test;
import org.seedstack.seed.spi.DependencyProvider;

public class DependencyModuleTest {
    @Test
    public void testConfigure(@Mocked final Binder binder, @Mocked final DependencyProvider myProvider) {
        Map<Class<?>, Optional<? extends DependencyProvider>> optionalDependencies = new HashMap<>();
        final Optional<DependencyProvider> maybe = Optional.of(myProvider);
        optionalDependencies.put(DependencyProvider.class, maybe);

        DependencyModule module = new DependencyModule(optionalDependencies);
        module.configure(binder);

        new Verifications() {
            {
                binder.bind(new TypeLiteral<Optional<DependencyProvider>>() {
                }).toInstance(maybe);
            }
        };
    }
}
