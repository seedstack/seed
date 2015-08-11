/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.mongodb;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoDatabase;
import org.bson.Document;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedIT;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class MongoDbAsyncIT extends AbstractSeedIT {
    @Inject
    @Named("client2")
    MongoClient client2;

    @Inject
    @Named("db2")
    MongoDatabase db2;

    @Inject
    @Named("client3")
    MongoClient client3;

    @Inject
    @Named("db3")
    MongoDatabase db3;

    @Test
    public void mongo_clients_are_injectable() {
        assertThat(client2).isNotNull();
        assertThat(client3).isNotNull();
    }

    @Test
    public void mongo_databases_are_injectable() {
        assertThat(db2).isNotNull();
        assertThat(db3).isNotNull();
    }

    @Test
    public void test_insert_into_collection() throws InterruptedException {
        Document doc = new Document("name", "MongoDB")
                .append("type", "database")
                .append("count", 1)
                .append("info", new Document("x", 203).append("y", 102));

        final Object mon = new Object();
        final AtomicBoolean inserted = new AtomicBoolean(false);
        db2.getCollection("test1").insertOne(doc, new SingleResultCallback<Void>() {
            @Override
            public void onResult(Void result, Throwable t) {
                assertThat(t).isNull();
                synchronized (mon) {
                    inserted.set(true);
                    mon.notify();
                }
            }
        });

        synchronized (mon) {
            mon.wait(5000);
            assertThat(inserted.get()).isTrue();
        }
    }
}
