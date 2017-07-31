/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.transaction;

import org.seedstack.seed.Ignore;
import org.seedstack.seed.transaction.spi.TransactionalLink;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * This proxy takes a {@link TransactionalLink} . It intercepts method calls and apply them
 * to the instance retrieved from {@link TransactionalLink}.get().
 *
 * @param <T> the type of the transactional object.
 */
@Ignore
public final class TransactionalProxy<T> implements InvocationHandler {
    private final TransactionalLink<T> transactionalLink;

    private TransactionalProxy(TransactionalLink<T> transactionalLink) {
        this.transactionalLink = transactionalLink;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(Object.class)) {
            return method.invoke(transactionalLink, args);
        }

        try {
            return method.invoke(transactionalLink.get(), args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * Create a transactional proxy around the provided {@link TransactionalLink}.
     *
     * @param clazz             the class representing the transacted resource.
     * @param transactionalLink the link to access the instance of the transacted resource.
     * @param <T>               type of the class representing the transacted resource.
     * @return the proxy.
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> clazz, TransactionalLink<T> transactionalLink) {
        return (T) Proxy.newProxyInstance(TransactionalProxy.class.getClassLoader(), new Class<?>[]{IgnoreAutoCloseable.class, clazz}, new TransactionalProxy<>(transactionalLink));
    }

    private interface IgnoreAutoCloseable extends AutoCloseable {
        @Override
        @Ignore
        void close() throws Exception;
    }
}