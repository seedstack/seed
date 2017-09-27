/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.dependency;

import static org.seedstack.shed.reflect.ReflectUtils.makeAccessible;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;

/**
 * Proxy to implement interfaces. Override each method to proxy (method from the interface).<br>
 * For example: to create a proxy for a Gauge interface
 *
 * <pre>
 * DependencyProxy&lt;Gauge&lt;Long&gt;&gt; pgauge = new DependencyProxy&lt;Gauge&lt;Long&gt;&gt;(new Class[]{Gauge
 * .class}, new ProxyMethodReplacer() {
 *   public Long getValue(){
 *      return RandomUtils.nextLong();
 *   }
 * });
 *
 * </pre>
 *
 * @param <T> class to proxy
 */
public class DependencyProxy<T> implements InvocationHandler {

    private final T proxy;
    private final Object substitute;

    /**
     * Create a proxy with all specified interfaces and override methods with the substitute object.
     *
     * @param interfaces interfaces for the proxy
     * @param substitute the method replacer to override method.
     */
    @SuppressWarnings("unchecked")
    public DependencyProxy(Class<?>[] interfaces, Object substitute) {
        try {
            this.proxy = (T) Proxy.newProxyInstance(DependencyProxy.class.getClassLoader(), interfaces, this);
        } catch (IllegalArgumentException e) {
            throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_CREATE_PROXY).put("class", interfaces[0].getName());
        }
        this.substitute = substitute;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method m;
        try {
            m = substitute.getClass().getMethod(method.getName(), method.getParameterTypes());
            makeAccessible(m);
        } catch (Exception e) {
            throw new UnsupportedOperationException(method.toString(), e);
        }
        try {
            return m.invoke(substitute, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    public T getProxy() {
        return proxy;
    }
}
