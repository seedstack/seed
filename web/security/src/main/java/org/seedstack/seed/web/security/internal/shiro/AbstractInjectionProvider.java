/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.security.internal.shiro;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.ProviderWithDependencies;

import java.lang.reflect.Constructor;
import java.util.Set;

class AbstractInjectionProvider<T> implements ProviderWithDependencies<T> {
    private Key<T> key;

    @Inject
    Injector injector;

    private InjectionPoint constructorInjectionPoint;
    private Set<Dependency<?>> dependencies;

    public AbstractInjectionProvider(Key<T> key) {
        this.key = key;
        constructorInjectionPoint = InjectionPoint.forConstructorOf(key.getTypeLiteral());

        ImmutableSet.Builder<Dependency<?>> dependencyBuilder = ImmutableSet.builder();
        dependencyBuilder.addAll(constructorInjectionPoint.getDependencies());
        for (InjectionPoint injectionPoint : InjectionPoint.forInstanceMethodsAndFields(key.getTypeLiteral())) {
            dependencyBuilder.addAll(injectionPoint.getDependencies());
        }
        this.dependencies = dependencyBuilder.build();
    }

    public T get() {
        Constructor<T> constructor = getConstructor();
        Object[] params = new Object[constructor.getParameterTypes().length];
        for (Dependency<?> dependency : constructorInjectionPoint.getDependencies()) {
            params[dependency.getParameterIndex()] = injector.getInstance(dependency.getKey());
        }
        T t;
        try {
            t = constructor.newInstance(params);
        } catch (Exception e) {
            throw new ProvisionException("Could not instantiate " + key + "", e);
        }
        injector.injectMembers(t);
        return postProcess(t);
    }

    @SuppressWarnings({"unchecked"})
    private Constructor<T> getConstructor() {
        return (Constructor<T>) constructorInjectionPoint.getMember();
    }

    protected T postProcess(T t) {
        // do nothing by default
        return t;
    }

    public Set<Dependency<?>> getDependencies() {
        return dependencies;
    }
}
