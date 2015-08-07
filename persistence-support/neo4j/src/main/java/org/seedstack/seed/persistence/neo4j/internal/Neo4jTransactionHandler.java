/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.neo4j.internal;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;


class Neo4jTransactionHandler implements TransactionHandler<Transaction> {
    private final GraphDatabaseServiceLink graphDatabaseServiceLink;
    private final GraphDatabaseService graphDatabaseService;

    Neo4jTransactionHandler(GraphDatabaseServiceLink graphDatabaseServiceLink, GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseServiceLink = graphDatabaseServiceLink;
        this.graphDatabaseService = graphDatabaseService;
    }

    @Override
    public void doInitialize(TransactionMetadata transactionMetadata) {
        // nothing to do
    }

    @Override
    public Transaction doCreateTransaction() {
        Transaction transaction = this.graphDatabaseService.beginTx();
        this.graphDatabaseServiceLink.push(graphDatabaseService, transaction);
        return transaction;
    }

    @Override
    public void doJoinGlobalTransaction() {
        // not supported
    }

    @Override
    public void doBeginTransaction(Transaction currentTransaction) {
        // nothing to do (transaction already began)
    }

    @Override
    public void doCommitTransaction(Transaction currentTransaction) {
        currentTransaction.success();
    }

    @Override
    public void doMarkTransactionAsRollbackOnly(Transaction currentTransaction) {
        // not supported
    }

    @Override
    public void doRollbackTransaction(Transaction currentTransaction) {
        currentTransaction.failure();
    }

    @Override
    public void doReleaseTransaction(Transaction currentTransaction) {
        currentTransaction.close();
        this.graphDatabaseServiceLink.pop();
    }

    @Override
    public void doCleanup() {
    }

    @Override
    public Transaction getCurrentTransaction() {
        return this.graphDatabaseServiceLink.getCurrentTransaction();
    }
}
