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


import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;

class InMemoryTransactionHandler implements TransactionHandler<Object> {
    private final InMemoryTransactionLink inMemoryTransactionLink;


    InMemoryTransactionHandler(InMemoryTransactionLink inMemoryTransactionLink) {
        this.inMemoryTransactionLink = inMemoryTransactionLink;
    }

    @Override
    public void doInitialize(TransactionMetadata transactionMetadata) {
        Object store = transactionMetadata.getMetadata("store");
        if (store != null) {
            inMemoryTransactionLink.set(store.toString());
        }

    }

    @Override
    public void doJoinGlobalTransaction() {
        throw new UnsupportedOperationException("In memory persistence doesn't support global transactions");
    }

    @Override
    public String doCreateTransaction() {
        return null;
    }

    @Override
    public void doBeginTransaction(Object currentTransaction) {
        // no transaction support
    }

    @Override
    public void doCommitTransaction(Object currentTransaction) {
        // no transaction support
    }

    @Override
    public void doMarkTransactionAsRollbackOnly(Object currentTransaction) {
        // no transaction support
    }

    @Override
    public void doRollbackTransaction(Object currentTransaction) {
        // no transaction support
    }

    @Override
    public void doReleaseTransaction(Object currentTransaction) {
        // no transaction support
    }

    @Override
    public void doCleanup() {
        // no transaction support
    }

    @Override
    public Object getCurrentTransaction() {
        return new Object();
    }
}
