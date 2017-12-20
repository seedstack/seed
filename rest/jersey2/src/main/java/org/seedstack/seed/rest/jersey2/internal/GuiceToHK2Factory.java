/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.jersey2.internal;

import com.google.inject.Injector;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * A generic HK2 factory backed by Guice.
 */
class GuiceToHK2Factory implements Factory {

    private final Class<?> componentClass;
    private final Injector injector;
    private final ServiceLocator serviceLocator;

    public GuiceToHK2Factory(Class<?> componentClass, Injector injector, ServiceLocator locator) {
        this.injector = injector;
        this.componentClass = componentClass;
        this.serviceLocator = locator;
    }

    @Override
    public Object provide() {
        Object instance = injector.getInstance(componentClass);
        if (instance != null) {
            serviceLocator.inject(instance);
        }
        return instance;
    }

    @Override
    public void dispose(Object instance) {
    }
}
