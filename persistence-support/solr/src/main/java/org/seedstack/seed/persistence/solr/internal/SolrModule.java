/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.solr.internal;

import com.google.inject.PrivateModule;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import org.apache.solr.client.solrj.SolrClient;
import org.seedstack.seed.persistence.solr.api.SolrExceptionHandler;
import org.seedstack.seed.transaction.utils.TransactionalClassProxy;

import java.util.Map;
import java.util.Map.Entry;

class SolrModule extends PrivateModule {
    private final Map<String, SolrClient> solrClients;
    private final Map<String, Class<? extends SolrExceptionHandler>> solrExceptionHandlers;

    SolrModule(Map<String, SolrClient> solrClients, Map<String, Class<? extends SolrExceptionHandler>> solrExceptionHandlers) {
        this.solrClients = solrClients;
        this.solrExceptionHandlers = solrExceptionHandlers;
    }

    @Override
    protected void configure() {
        SolrClientLink solrClientLink = new SolrClientLink();
        bind(SolrClient.class).toInstance(TransactionalClassProxy.create(SolrClient.class, solrClientLink));

        for (Entry<String, SolrClient> solrClientEntry : solrClients.entrySet()) {
            bindSolrClient(solrClientEntry.getKey(), solrClientEntry.getValue(), solrClientLink);
        }

        expose(SolrClient.class);
    }

    private void bindSolrClient(String clientName, SolrClient solrClient, SolrClientLink solrClientLink) {
        Class<? extends SolrExceptionHandler> unitExceptionHandlerClass = solrExceptionHandlers.get(clientName);

        if (unitExceptionHandlerClass != null) {
            bind(SolrExceptionHandler.class)
                    .annotatedWith(Names.named(clientName))
                    .to(unitExceptionHandlerClass);
        } else {
            bind(SolrExceptionHandler.class)
                    .annotatedWith(Names.named(clientName))
                    .toProvider(Providers.<SolrExceptionHandler>of(null));
        }

        bind(SolrTransactionHandler.class)
                .annotatedWith(Names.named(clientName))
                .toInstance(new SolrTransactionHandler(solrClientLink, solrClient));

        bind(SolrClient.class)
                .annotatedWith(Names.named(clientName))
                .toInstance(solrClient);

        expose(SolrExceptionHandler.class).annotatedWith(Names.named(clientName));
        expose(SolrTransactionHandler.class).annotatedWith(Names.named(clientName));
        expose(SolrClient.class).annotatedWith(Names.named(clientName));
    }
}
