/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.elasticsearch;

import org.assertj.core.api.Assertions;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.json.JSONException;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.it.AbstractSeedIT;
import org.seedstack.seed.it.internal.categories.NotSelfContained;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Date;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Category(NotSelfContained.class)
public class ElasticSearchIT extends AbstractSeedIT {
    @Inject
    @Named("client1")
    Client remoteClient;

    @Inject
    @Named("client2")
    Client inMemoryClient;

    @Test
    public void clients_are_injectable() {
        Assertions.assertThat(remoteClient).isNotNull();
        Assertions.assertThat(inMemoryClient).isNotNull();
    }

    @Test(expected = SeedException.class)
    public void assert_inmemory_client_close_exception() {
        inMemoryClient.close();
    }

    @Test(expected = SeedException.class)
    public void assert_remote_client_close_exception() {
        remoteClient.close();
    }

    @Test
    public void remote_indexing_and_searching() throws ElasticsearchException, IOException, JSONException {
        indexing_and_searching(remoteClient);
    }

    @Test
    public void inmemory_indexing_and_searching() throws ElasticsearchException, IOException, JSONException {
        indexing_and_searching(inMemoryClient);
    }

    public void indexing_and_searching(Client client) throws ElasticsearchException, IOException, JSONException {
        XContentBuilder xContentBuilder = jsonBuilder()
                .startObject()
                .field("user", "seed")
                .field("postDate", new Date())
                .field("message", "trying out Elasticsearch")
                .endObject();

        client.prepareIndex("index1", "mapping1", "1")
                .setSource(xContentBuilder)
                .execute()
                .actionGet();

        GetResponse response = client.prepareGet("index1", "mapping1", "1")
                .execute()
                .actionGet();

        response.getFields();
        Assertions.assertThat(response.isExists()).isTrue();
        JSONAssert.assertEquals(xContentBuilder.string(), response.getSourceAsString(), true);
    }
}
