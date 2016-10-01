/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

import org.seedstack.shed.exception.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Proxy to implement interfaces. Override each method to proxy (method from the interface).<br>
 * For example: to create a proxy for a Gauge interface
 * 
 * <pre>
 * DependencyProxy&lt;Gauge&lt;Long&gt;&gt; pgauge = new DependencyProxy&lt;Gauge&lt;Long&gt;&gt;(new Class[]{Gauge.class}, new ProxyMethodReplacer() {
 *   public Long getValue(){
 *      return RandomUtils.nextLong();
 *   }
 * });
 *
 * </pre>
 * @author thierry.bouvet@mpsa.com
 *
 * @param <T> class to proxy
 */
public class DependencyProxy<T> implements InvocationHandler{

	private final T proxy;
	private final ProxyMethodReplacer methodReplacer;
	
	/**
	 * Create a proxy with all specified interfaces and override methods with the {@link ProxyMethodReplacer}.
	 * 
	 * @param interfaces interfaces for the proxy
	 * @param methodReplacer the method replacer to override method.
	 */
	@SuppressWarnings("unchecked")
	public DependencyProxy(Class<?>[] interfaces, ProxyMethodReplacer methodReplacer) {
		try {
			this.proxy = (T) Proxy.newProxyInstance(DependencyProxy.class.getClassLoader(), interfaces, this);
		} catch (IllegalArgumentException e) {
            throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_CREATE_PROXY).put("class", interfaces[0].getName());
		}
		this.methodReplacer = methodReplacer;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method m;
        try {
            m = methodReplacer.getClass().getMethod(method.getName(), method.getParameterTypes());
            m.setAccessible(true);
        } catch (Exception e) {
            throw new UnsupportedOperationException(method.toString(), e);
        }
        try {
            return m.invoke(methodReplacer, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
	}

	public T getProxy() {
		return proxy;
	}
	
	
}
