/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.mongodb.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;

import java.util.Map;

class MongoDbModule<C, D> extends AbstractModule {
    private final Map<String, C> mongoClients;
    private final Map<String, D> mongoDatabases;
    private final Class<C> clientClass;
    private final Class<D> dbClass;

    MongoDbModule(Class<C> clientClass, Class<D> dbClass, Map<String, C> mongoClients, Map<String, D> mongoDatabases) {
        this.clientClass = clientClass;
        this.dbClass = dbClass;
        this.mongoClients = mongoClients;
        this.mongoDatabases = mongoDatabases;
    }

    @Override
    protected void configure() {
        // bind clients
        for (Map.Entry<String, C> mongoClientEntry : mongoClients.entrySet()) {
            bind(clientClass)
                    .annotatedWith(Names.named(mongoClientEntry.getKey()))
                    .toInstance(mongoClientEntry.getValue());
        }

        // if only one client is defined, define a linked binding without qualifier to it
        if (mongoClients.size() == 1) {
            bind(clientClass)
                    .to(Key.get(clientClass, Names.named(mongoClients.keySet().iterator().next())));
        }

        // bind databases
        for (Map.Entry<String, D> mongoDatabaseEntry : mongoDatabases.entrySet()) {
            bind(dbClass)
                    .annotatedWith(Names.named(mongoDatabaseEntry.getKey()))
                    .toInstance(mongoDatabaseEntry.getValue());
        }

        // if only one database is defined, define a linked binding without qualifier to it
        if (mongoDatabases.size() == 1) {
            bind(dbClass)
                    .to(Key.get(dbClass, Names.named(mongoDatabases.keySet().iterator().next())));
        }
    }
}
