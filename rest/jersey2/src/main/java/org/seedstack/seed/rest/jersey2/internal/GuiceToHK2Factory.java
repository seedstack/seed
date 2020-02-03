/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.internal;

import com.google.inject.Injector;
import com.google.inject.Key;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.seedstack.shed.reflect.Classes;

/**
 * A generic HK2 factory backed by Guice.
 */
class GuiceToHK2Factory<T> implements Factory<T> {
    private final Class<T> componentClass;
    private final Injector injector;
    private final ServiceLocator serviceLocator;
    private final boolean guice;

    GuiceToHK2Factory(Class<T> componentClass, Injector injector, ServiceLocator locator) {
        this.injector = injector;
        this.componentClass = componentClass;
        this.serviceLocator = locator;
        this.guice = this.injector.getExistingBinding(Key.get(componentClass)) != null;
    }

    @Override
    public T provide() {
        T instance;
        if (guice) {
            instance = injector.getInstance(componentClass);
        } else {
            instance = Classes.instantiateDefault(componentClass);
        }
        if (instance != null) {
            serviceLocator.inject(instance);
        }
        return instance;
    }

    @Override
    public void dispose(Object instance) {
    }
}
