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

import com.google.inject.PrivateModule;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import org.neo4j.graphdb.GraphDatabaseService;
import org.seedstack.seed.persistence.neo4j.api.Neo4jExceptionHandler;
import org.seedstack.seed.transaction.spi.TransactionConcern;
import org.seedstack.seed.transaction.utils.TransactionalProxy;

import java.util.Map;

@TransactionConcern
class Neo4jModule extends PrivateModule {
    private final Map<String, GraphDatabaseService> graphDatabaseServices;
    private final Map<String, Class<? extends Neo4jExceptionHandler>> exceptionHandlerClasses;

    Neo4jModule(Map<String, GraphDatabaseService> graphDatabaseServices, Map<String, Class<? extends Neo4jExceptionHandler>> exceptionHandlerClasses) {
        this.graphDatabaseServices = graphDatabaseServices;
        this.exceptionHandlerClasses = exceptionHandlerClasses;
    }

    @Override
    protected void configure() {
        GraphDatabaseServiceLink graphDatabaseServiceLink = new GraphDatabaseServiceLink();
        bind(GraphDatabaseService.class).toInstance(TransactionalProxy.create(GraphDatabaseService.class, graphDatabaseServiceLink));

        for (Map.Entry<String, GraphDatabaseService> entry : graphDatabaseServices.entrySet()) {
            bindGraphDatabase(entry.getKey(), entry.getValue(), graphDatabaseServiceLink);
        }

        expose(GraphDatabaseService.class);
    }

    private void bindGraphDatabase(String name, GraphDatabaseService graphDatabaseService, GraphDatabaseServiceLink graphDatabaseServiceLink) {
        Class<? extends Neo4jExceptionHandler> exceptionHandlerClass = exceptionHandlerClasses.get(name);

        if (exceptionHandlerClass != null) {
            bind(Neo4jExceptionHandler.class).annotatedWith(Names.named(name)).to(exceptionHandlerClass);
        } else {
            bind(Neo4jExceptionHandler.class).annotatedWith(Names.named(name)).toProvider(Providers.<Neo4jExceptionHandler>of(null));
        }

        Neo4jTransactionHandler transactionHandler = new Neo4jTransactionHandler(graphDatabaseServiceLink, graphDatabaseService);
        bind(Neo4jTransactionHandler.class).annotatedWith(Names.named(name)).toInstance(transactionHandler);

        expose(Neo4jExceptionHandler.class).annotatedWith(Names.named(name));
        expose(Neo4jTransactionHandler.class).annotatedWith(Names.named(name));
    }

}
