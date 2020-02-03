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
import java.util.Collection;
import java.util.Set;

class CoreModule extends AbstractModule {
    private final Collection<? extends Module> modules;
    private final Set<Bindable> bindings;

    CoreModule(Collection<? extends Module> modules, Set<Bindable> bindings) {
        this.modules = modules;
        this.bindings = bindings;
    }

    @Override
    protected void configure() {
        modules.forEach(this::install);
        bindings.forEach(binding -> binding.apply(binder()));
    }
}
