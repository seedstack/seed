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
import org.seedstack.seed.LifecycleListener;
import org.seedstack.seed.spi.dependency.DependencyProvider;
import org.seedstack.seed.spi.dependency.Maybe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Unit test for {@link CoreModule}.
 *
 * @author thierry.bouvet@mpsa.com
 */
public class CoreModuleTest {

    /**
     * Test optional dependencies.
     *
     * @param binder mock binder
     */
    @Test
    public void testConfigure(@Mocked final Binder binder, @Mocked final DependencyProvider myProvider) {
        Collection<Module> subModules = new ArrayList<Module>();

        Map<Class<?>, Maybe<? extends DependencyProvider>> optionalDependencies = new HashMap<Class<?>, Maybe<? extends DependencyProvider>>();
        final Maybe<DependencyProvider> maybe = new Maybe<DependencyProvider>(myProvider);
        optionalDependencies.put(DependencyProvider.class, maybe);

        CoreModule module = new CoreModule(subModules, null, null, optionalDependencies);
        module.configure(binder);

        new Verifications() {
            {
                binder.bind(new TypeLiteral<Maybe<DependencyProvider>>() {}).toInstance(maybe);
            }
        };
    }
}
