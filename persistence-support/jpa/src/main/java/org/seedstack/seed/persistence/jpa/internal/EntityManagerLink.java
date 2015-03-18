/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.jpa.internal;

import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.transaction.spi.TransactionalLink;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.ArrayDeque;
import java.util.Deque;

class EntityManagerLink implements TransactionalLink<EntityManager> {
    private final ThreadLocal<Deque<EntityManager>> perThreadObjectContainer = new ThreadLocal<Deque<EntityManager>>() {
        @Override
        protected Deque<EntityManager> initialValue() {
            return new ArrayDeque<EntityManager>();
        }
    };

    @Override
    public EntityManager get() {
        EntityManager entityManager = this.perThreadObjectContainer.get().peek();

        if (entityManager == null) {
            throw SeedException.createNew(JpaErrorCode.ACCESSING_ENTITY_MANAGER_OUTSIDE_TRANSACTION);
        }

        return entityManager;
    }

    EntityTransaction getCurrentTransaction() {
        EntityManager entityManager = this.perThreadObjectContainer.get().peek();

        if (entityManager != null) {
            return entityManager.getTransaction();
        } else {
            return null;
        }
    }

    void push(EntityManager entityManager) {
    	perThreadObjectContainer.get().push(entityManager);
    }

    EntityManager pop() {
        return perThreadObjectContainer.get().pop();
    }
}
