/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */
package org.seedstack.seed.persistence.inmemory.internal;

import org.seedstack.seed.transaction.spi.TransactionalLink;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author redouane.loulou@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 */
class InMemoryTransactionLink implements TransactionalLink<String> {
    private final ThreadLocal<Deque<String>> perThreadObjectContainer = new ThreadLocal<Deque<String>>() {
        @Override
        protected Deque<String> initialValue() {
            return new ArrayDeque<String>();
        }
    };

    @Override
    public String get() {
        String entityManager = this.perThreadObjectContainer.get().peek();

        if (entityManager == null) {
            throw new IllegalStateException("A store must be specified with @Store before accessing in memory map");
        }

        return entityManager;
    }

    void push(String entityManager) {
        perThreadObjectContainer.get().push(entityManager);
    }

    String pop() {
        return perThreadObjectContainer.get().pop();
    }
}
