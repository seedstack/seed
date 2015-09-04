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
package org.seedstack.seed.persistence.solr.internal;

import org.apache.solr.client.solrj.SolrClient;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.persistence.solr.api.SolrErrorCodes;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;

class SolrTransactionHandler implements TransactionHandler<SolrClient> {
    private final SolrClientLink solrClientLink;
    private final SolrClient solrClient;

    SolrTransactionHandler(SolrClientLink solrClientLink, SolrClient solrClient) {
        super();
        this.solrClientLink = solrClientLink;
        this.solrClient = solrClient;
    }

    @Override
    public void doInitialize(TransactionMetadata transactionMetadata) {
        // nothing to do
    }

    @Override
    public SolrClient doCreateTransaction() {
        solrClientLink.push(solrClient);
        return solrClient;
    }

    @Override
    public void doBeginTransaction(SolrClient currentTransaction) {
        // nothing to do
    }

    @Override
    public void doCommitTransaction(SolrClient currentTransaction) {
        try {
            solrClientLink.pop().commit();
        } catch (Exception e) {
            throw SeedException.wrap(e, SolrErrorCodes.UNABLE_TO_COMMIT);
        }
    }

    @Override
    public void doRollbackTransaction(SolrClient currentTransaction) {
        try {
            solrClientLink.pop().rollback();
        } catch (Exception e) {
            throw SeedException.wrap(e, SolrErrorCodes.UNABLE_TO_ROLLBACK);
        }
    }

    @Override
    public void doReleaseTransaction(SolrClient currentTransaction) {
        // nothing to do
    }

    @Override
    public void doCleanup() {
        // nothing to do
    }

    @Override
    public SolrClient getCurrentTransaction() {
        return solrClientLink.getCurrentClient();
    }

    @Override
    public void doJoinGlobalTransaction() {
        // not supported
    }

    @Override
    public void doMarkTransactionAsRollbackOnly(SolrClient currentTransaction) {
        // not supported
    }
}
