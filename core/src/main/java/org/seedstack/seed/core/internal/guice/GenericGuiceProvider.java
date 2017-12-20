/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.guice;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import java.lang.reflect.Type;
import javax.inject.Inject;

/**
 * This class is a generic provider.
 *
 * It passes an array of object to the provided object constructor. This array contains the generic types of
 * the created object. It will balance the fact that we won't be able to use reflection to get the generic type
 * on the created object.
 *
 * @param <T> Type to get from the generic provider.
 */
public class GenericGuiceProvider<T> implements Provider<T> {
    private final Class<?> defaultImplClass;
    private Type[] genericClasses;

    @Inject
    private Injector injector;

    /**
     * Constructs a provider for a default repository of a given aggregate.
     *
     * @param defaultImplClass the default implementation class
     * @param genericClasses   generic array classes
     */
    public GenericGuiceProvider(Class<?> defaultImplClass, Type... genericClasses) {
        this.defaultImplClass = defaultImplClass;
        this.genericClasses = genericClasses;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get() {
        Key<GenericGuiceFactory<T>> factoryKey = (Key<GenericGuiceFactory<T>>) Key.get(
                TypeLiteral.get(
                        Types.newParameterizedType(GenericGuiceFactory.class, defaultImplClass)
                ));
        GenericGuiceFactory<T> genericGuiceFactory = injector.getInstance(factoryKey);
        return genericGuiceFactory.createResolvedInstance(genericClasses);
    }
}
