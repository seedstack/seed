/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.elasticsearch.internal;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.name.Names;
import org.elasticsearch.client.Client;

import java.util.Map;
import java.util.Map.Entry;

class ElasticSearchModule extends PrivateModule {
    private Map<String, Client> elasticSearchClientWrappers;

    ElasticSearchModule(Map<String, Client> elasticSearchClientWrappers) {
        this.elasticSearchClientWrappers = elasticSearchClientWrappers;
    }

    @Override
    protected void configure() {
        if (elasticSearchClientWrappers != null && !elasticSearchClientWrappers.isEmpty()) {
            for (Entry<String, Client> entry : elasticSearchClientWrappers.entrySet()) {
                Key<Client> clientKey = Key.get(Client.class, Names.named(entry.getKey()));
                bind(clientKey).toInstance(ElasticSearchClientProxy.create(Client.class, entry.getValue()));
                expose(clientKey);
            }
        }
    }
}
