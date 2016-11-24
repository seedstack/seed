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

import java.util.Collection;

class CoreModule extends AbstractModule {
    private final Collection<Module> subModules;

    CoreModule(Collection<Module> subModules) {
        this.subModules = subModules;
    }

    @Override
    protected void configure() {
        subModules.forEach(this::install);
    }
}
