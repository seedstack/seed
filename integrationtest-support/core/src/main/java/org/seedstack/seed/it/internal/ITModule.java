/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it.internal;

import com.google.inject.AbstractModule;

import java.util.Collection;

class ITModule extends AbstractModule {
    private final Collection<Class<?>> iTs;
    private final Class<?> testClass;

    ITModule(Class<?> testClass, Collection<Class<?>> iTs) {
        this.iTs = iTs;
        this.testClass = testClass;
    }

    @Override
    protected void configure() {
        if (testClass != null) {
            bind(testClass);
        }

        if (iTs != null && !iTs.isEmpty()) {
            for (Class<?> iT : iTs) {
                bind(iT);
            }
        }
    }
}
