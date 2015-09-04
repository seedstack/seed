/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.elasticsearch.internal;

import org.seedstack.seed.core.api.SeedException;
import org.elasticsearch.client.Client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * ElasticSearch client proxy, forbid the use of the close method
 * Clients can only be closed by the plugin if managed by seed.
 *
 * @author redouane.loulou@ext.mpsa.com
 */
final class ElasticSearchClientProxy implements InvocationHandler {
    private static final String METHOD_CLOSE = "close";

    private final Client client;

    private ElasticSearchClientProxy(Client client) {
        this.client = client;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable { // NOSONAR
        if (METHOD_CLOSE.contentEquals(method.getName())) {
            throw SeedException.createNew(ElasticSearchErrorCode.FORBIDDEN_CLIENT_CLOSE);
        }

        try {
            return method.invoke(client, args);
        } catch (InvocationTargetException e) { // NOSONAR
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        }
    }

    /**
     * Create a transactional proxy around the provided {@link org.seedstack.seed.transaction.spi.TransactionalLink}.
     *
     * @param clazz  the class representing the transacted resource.
     * @param client the link to access the instance of the transacted resource.
     * @param <T>    type of the class representing the transacted resource.
     * @return the proxy.
     */
    @SuppressWarnings("unchecked")
    static <T> T create(Class<T> clazz, Client client) {
        return (T) Proxy.newProxyInstance(ElasticSearchClientProxy.class.getClassLoader(), new Class<?>[]{clazz}, new ElasticSearchClientProxy(client));
    }
}
