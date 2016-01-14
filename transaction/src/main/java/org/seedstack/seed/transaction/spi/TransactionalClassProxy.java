/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.transaction.spi;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.transaction.internal.TransactionErrorCode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class proxy takes a {@link org.seedstack.seed.transaction.spi.TransactionalLink}. It intercepts method calls and
 * apply them to the instance retrieved from {@link org.seedstack.seed.transaction.spi.TransactionalLink}.get().
 *
 * @param <T> the type of the transactional object.
 * @author redouane.loulou@ext.mpsa.com
 */
public class TransactionalClassProxy<T> implements MethodHandler {
    private TransactionalLink<T> transactionalLink;

    private TransactionalClassProxy(TransactionalLink<T> transactionalLink) {
        super();
        this.transactionalLink = transactionalLink;
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        try {
            return thisMethod.invoke(transactionalLink.get(), args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * Create a transactional proxy around the provided {@link TransactionalLink}.
     *
     * @param <T>               the interface used to create the proxy.
     * @param clazz             the class representing the transacted resource.
     * @param transactionalLink the link to access the instance of the transacted resource.
     * @return the proxy.
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> clazz, final TransactionalLink<T> transactionalLink) {
        try {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(clazz);
            factory.setFilter(new MethodFilter() {
                @Override
                public boolean isHandled(Method method) {
                    return !method.getDeclaringClass().equals(Object.class);
                }
            });
            return (T) factory.create(new Class<?>[0], new Object[0], new TransactionalClassProxy<T>(transactionalLink));
        } catch (Exception e) {
            throw SeedException.wrap(e, TransactionErrorCode.UNABLE_TO_CREATE_TRANSACTIONAL_PROXY).put("class", clazz.getName());
        }
    }
}
