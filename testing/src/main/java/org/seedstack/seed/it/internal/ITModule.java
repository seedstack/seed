/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.seedstack.seed.it.internal.arquillian.InjectionEnricher;

import java.util.Collection;
import java.util.Set;

class ITModule extends AbstractModule {
    private final Collection<Class<?>> iTs;
    private final Class<?> testClass;
    private final Set<Module> itInstallModules;

    ITModule(Class<?> testClass, Collection<Class<?>> iTs, Set<Module> itInstallModules) {
        this.iTs = iTs;
        this.testClass = testClass;
        this.itInstallModules = itInstallModules;
    }

    @Override
    protected void configure() {
        requestStaticInjection(InjectionEnricher.class);

        if (testClass != null) {
            bind(testClass);
        }

        if (itInstallModules != null) {
            for (Module itInstallModule : itInstallModules) {
                install(itInstallModule);
            }
        }

        if (iTs != null) {
            for (Class<?> iT : iTs) {
                bind(iT);
            }
        }
    }
}
