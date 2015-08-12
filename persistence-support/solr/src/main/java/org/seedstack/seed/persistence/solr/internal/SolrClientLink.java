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
import org.seedstack.seed.transaction.spi.TransactionalLink;

import java.util.ArrayDeque;
import java.util.Deque;

class SolrClientLink implements TransactionalLink<SolrClient> {
    private final ThreadLocal<Deque<SolrClient>> perThreadObjectContainer = new ThreadLocal<Deque<SolrClient>>() {
        @Override
        protected Deque<SolrClient> initialValue() {
            return new ArrayDeque<SolrClient>();
        }
    };

    @Override
    public SolrClient get() {
        SolrClient solrClient = this.perThreadObjectContainer.get().peek();

        if (solrClient == null) {
            throw SeedException.createNew(SolrErrorCodes.ACCESSING_SOLR_CLIENT_OUTSIDE_TRANSACTION);
        }

        return solrClient;
    }

    void push(SolrClient solrClient) {
        perThreadObjectContainer.get().push(solrClient);
    }

    SolrClient pop() {
        return perThreadObjectContainer.get().pop();
    }

    SolrClient getCurrentClient() {
        SolrClient solrClient = this.perThreadObjectContainer.get().peek();

        if (solrClient != null) {
            return solrClient;
        } else {
            return null;
        }
    }
}
