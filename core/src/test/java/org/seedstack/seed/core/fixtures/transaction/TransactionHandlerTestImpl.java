/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.fixtures.transaction;

import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;

/**
 * test implementation of TransactionHandler
 */
public class TransactionHandlerTestImpl implements TransactionHandler<Object> {

    private Object transaction;

    @Override
    public void doInitialize(TransactionMetadata transactionMetadata) {
    }

    @Override
    public Object doCreateTransaction() {
        return transaction = new Object();
    }

    @Override
    public void doJoinGlobalTransaction() {
    }

    @Override
    public void doBeginTransaction(Object currentTransaction) {
    }

    @Override
    public void doCommitTransaction(Object currentTransaction) {
    }

    @Override
    public void doMarkTransactionAsRollbackOnly(Object currentTransaction) {
    }

    @Override
    public void doRollbackTransaction(Object currentTransaction) {
    }

    @Override
    public void doReleaseTransaction(Object currentTransaction) {
    }

    @Override
    public void doCleanup() {
    }

    @Override
    public Object getCurrentTransaction() {
        return transaction;
    }
}
