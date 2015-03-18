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

import com.google.inject.PrivateModule;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import org.seedstack.seed.persistence.tinkerpop.api.GraphExceptionHandler;
import org.seedstack.seed.transaction.spi.TransactionConcern;
import org.seedstack.seed.transaction.utils.TransactionalProxy;

import java.util.Map;

@TransactionConcern
class TinkerpopModule extends PrivateModule {
    private final Map<String, com.tinkerpop.blueprints.Graph> graphs;
    private final Map<String, Class<? extends GraphExceptionHandler>> graphExceptionHandlerClasses;
    private final FramedGraphFactory framedGraphFactory;

    TinkerpopModule(Map<String, Graph> graphs, Map<String, Class<? extends GraphExceptionHandler>> graphExceptionHandlerClasses, FramedGraphFactory framedGraphFactory) {
        this.graphs = graphs;
        this.graphExceptionHandlerClasses = graphExceptionHandlerClasses;
        this.framedGraphFactory = framedGraphFactory;
    }

    @Override
    protected void configure() {
        GraphLink graphLink = new GraphLink();
        Graph seedGraph = TransactionalProxy.create(Graph.class, graphLink);

        bind(Graph.class).toInstance(seedGraph);
        bind(FramedGraph.class).toInstance(framedGraphFactory.create(seedGraph));

        for (Map.Entry<String, com.tinkerpop.blueprints.Graph> entry : graphs.entrySet()) {
            bindGraph(entry.getKey(), entry.getValue(), graphLink);
        }

        expose(Graph.class);
        expose(FramedGraph.class);
    }

    private void bindGraph(String name, com.tinkerpop.blueprints.Graph graph, GraphLink graphLink) {
        Class<? extends GraphExceptionHandler> graphExceptionHandlerClass = graphExceptionHandlerClasses.get(name);

        if (graphExceptionHandlerClass != null) {
            bind(GraphExceptionHandler.class).annotatedWith(Names.named(name)).to(graphExceptionHandlerClass);
        } else {
            bind(GraphExceptionHandler.class).annotatedWith(Names.named(name)).toProvider(Providers.<GraphExceptionHandler>of(null));
        }

        GraphTransactionHandler transactionHandler = new GraphTransactionHandler(graphLink, graph);
        bind(GraphTransactionHandler.class).annotatedWith(Names.named(name)).toInstance(transactionHandler);

        expose(GraphExceptionHandler.class).annotatedWith(Names.named(name));
        expose(GraphTransactionHandler.class).annotatedWith(Names.named(name));
    }
}
