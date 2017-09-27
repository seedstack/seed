/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.dependency;

import static org.seedstack.shed.reflect.ReflectUtils.makeAccessible;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;

/**
 * Proxy to implement abstract class. Override methods to proxy.<br>
 * For example: to create a proxy for a RatioGauge abstract class
 *
 * <pre>
 * DependencyClassProxy&lt;RatioGauge&gt; ratio = new DependencyClassProxy&lt;RatioGauge&gt;(RatioGauge.class, new
 * ProxyMethodReplacer() {
 *    public Ratio getRatio() {
 *        return Ratio.of(2, 1);
 *    }
 * });
 *
 * </pre>
 *
 * @param <T> class to proxy
 */
public class DependencyClassProxy<T> implements MethodHandler {
    private final T proxy;
    private final Object substitute;

    /**
     * Create a proxy replacing methods of a class.
     *
     * @param clazz      the class to be proxied.
     * @param substitute the substitute implementation.
     */
    @SuppressWarnings("unchecked")
    public DependencyClassProxy(Class<T> clazz, final Object substitute) {
        this.substitute = substitute;
        try {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(clazz);
            factory.setFilter(method -> {
                for (Method m : substitute.getClass().getDeclaredMethods()) {
                    if (m.getName().equals(method.getName()))
                        return true;
                }
                return false;
            });
            this.proxy = (T) factory.create(new Class<?>[0], new Object[0], this);
        } catch (Exception e) {
            throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_CREATE_PROXY).put("class", clazz.getName());
        }
    }

    public T getProxy() {
        return proxy;
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        try {
            return makeAccessible(
                    substitute.getClass().getMethod(thisMethod.getName(), thisMethod.getParameterTypes())).invoke(
                    substitute, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
