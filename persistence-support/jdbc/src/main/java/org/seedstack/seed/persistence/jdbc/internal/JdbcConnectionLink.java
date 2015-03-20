/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 17 févr. 2015
 */
package org.seedstack.seed.persistence.jdbc.internal;

import java.sql.Connection;
import java.util.ArrayDeque;
import java.util.Deque;

import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.transaction.spi.TransactionalLink;

/**
 * Link for JDBC connection
 */
public class JdbcConnectionLink implements TransactionalLink<Connection> {

    private final ThreadLocal<Deque<JdbcTransaction>> perThreadObjectContainer = new ThreadLocal<Deque<JdbcTransaction>>() {
        @Override
        protected Deque<JdbcTransaction> initialValue() {
            return new ArrayDeque<JdbcTransaction>();
        }
    };

    @Override
    public Connection get() {
        JdbcTransaction transaction = this.perThreadObjectContainer.get().peek();

        if (transaction == null) {
            throw SeedException.createNew(JdbcErrorCode.ACCESSING_JDBC_CONNECTION_OUTSIDE_TRANSACTION);
        }

        return transaction.getConnection();
    }

    Connection getCurrentConnection() {
        JdbcTransaction currentTransaction = getCurrentTransaction();
        if (currentTransaction == null)
            return null;
        return currentTransaction.getConnection();
    }

    JdbcTransaction getCurrentTransaction() {
        return this.perThreadObjectContainer.get().peek();
    }

    void push(JdbcTransaction transaction) {
        perThreadObjectContainer.get().push(transaction);
    }

    JdbcTransaction pop() {
        return perThreadObjectContainer.get().pop();
    }

    boolean isLastTransaction() {
        return perThreadObjectContainer.get().size() == 1;
    }
}
