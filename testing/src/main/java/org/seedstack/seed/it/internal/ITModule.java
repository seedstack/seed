/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.it.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import java.util.Collection;
import java.util.Set;
import org.seedstack.seed.core.internal.BindingDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ITModule extends AbstractModule {
    private final Logger LOGGER = LoggerFactory.getLogger(ITModule.class);
    private final Class<?> testClass;
    private final Collection<? extends Module> modules;
    private final Set<BindingDefinition> bindings;
    private final boolean overriding;

    ITModule(Class<?> testClass, Collection<? extends Module> modules, Set<BindingDefinition> bindings,
            boolean overriding) {
        this.testClass = testClass;
        this.modules = modules;
        this.bindings = bindings;
        this.overriding = overriding;
    }

    @Override
    protected void configure() {
        if (testClass != null) {
            bind(testClass);
        }

        modules.forEach(module -> {
            LOGGER.trace("Installing module {}", module.getClass().getName());
            install(module);
        });
        LOGGER.debug("Installed {}{} test module(s)", modules.size(), overriding ? " overriding" : "");

        bindings.forEach(binding -> binding.apply(binder()));
        LOGGER.debug("Created {}{} test binding(s)", bindings.size(), overriding ? " overriding" : "");
    }
}
