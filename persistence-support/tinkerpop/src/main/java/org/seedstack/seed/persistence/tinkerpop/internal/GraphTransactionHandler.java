/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.tinkerpop.internal;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.ThreadedTransactionalGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;

class GraphTransactionHandler implements TransactionHandler<Graph> {
    private final Graph graph;
    private final GraphLink graphLink;

    GraphTransactionHandler(GraphLink graphLink, Graph graph) {
        this.graphLink = graphLink;
        this.graph = graph;
    }

    @Override
    public void doInitialize(TransactionMetadata transactionMetadata) {
        // nothing to do here
    }

    @Override
    public void doJoinGlobalTransaction() {
        throw new UnsupportedOperationException("TinkerPop persistence implementation doesn't support global transactions");
    }

    @Override
    public Graph doCreateTransaction() {
        if (graph instanceof ThreadedTransactionalGraph) {
            TransactionalGraph tg = ((ThreadedTransactionalGraph) graph).newTransaction();
            this.graphLink.push(tg);
            return tg;
        } else {
            this.graphLink.push(graph);
            return graph;
        }
    }

    @Override
    public void doBeginTransaction(Graph currentTransaction) {
        // nothing to do here (a transaction starts automatically)
    }

    @Override
    public void doCommitTransaction(Graph currentTransaction) {
        if (currentTransaction instanceof TransactionalGraph) {
            ((TransactionalGraph) currentTransaction).commit();
        }
    }

    @Override
    public void doMarkTransactionAsRollbackOnly(Graph currentTransaction) {
        // nothing to do
    }

    @Override
    public void doRollbackTransaction(Graph currentTransaction) {
        if (currentTransaction instanceof TransactionalGraph) {
            ((TransactionalGraph) graph).rollback();
        }
    }

    @Override
    public void doReleaseTransaction(Graph currentTransaction) {
        // nothing to do here (a transaction is automatically released)
    }

    @Override
    public void doCleanup() {
        this.graphLink.pop();
    }

    @Override
    public Graph getCurrentTransaction() {
        return this.graphLink.getCurrentTransaction();
    }
}
