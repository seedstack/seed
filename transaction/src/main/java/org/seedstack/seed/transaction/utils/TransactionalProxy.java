/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.transaction.utils;

import org.seedstack.seed.transaction.spi.TransactionalLink;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * This proxy takes a {@link org.seedstack.seed.transaction.spi.TransactionalLink} . It intercepts method calls and apply them
 * to the instance retrieved from {@link org.seedstack.seed.transaction.spi.TransactionalLink}.get().
 *
 * @author adrien.lauer@mpsa.com
 * @param <T> the type of the transactional object.
 */
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
        } catch(InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * Create a transactional proxy around the provided {@link TransactionalLink}.
     *
     * @param clazz the class representing the transacted resource.
     * @param transactionalLink the link to access the instance of the transacted resource.
     * @param <T> type of the class representing the transacted resource.
     * @return the proxy.
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> clazz, TransactionalLink<T> transactionalLink) {
        return (T) Proxy.newProxyInstance(TransactionalProxy.class.getClassLoader(), new Class<?>[]{clazz}, new TransactionalProxy<T>(transactionalLink));
    }
}