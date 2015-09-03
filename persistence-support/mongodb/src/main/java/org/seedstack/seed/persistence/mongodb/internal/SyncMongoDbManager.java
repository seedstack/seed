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

import com.mongodb.DBDecoderFactory;
import com.mongodb.DBEncoderFactory;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.configuration.Configuration;
import org.bson.codecs.configuration.CodecRegistry;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.persistence.mongodb.api.MongoDbErrorCodes;

import javax.net.SocketFactory;
import java.util.Iterator;
import java.util.List;

class SyncMongoDbManager extends AbstractMongoDbManager<MongoClient, MongoDatabase> {
    @Override
    protected MongoClient doCreateClient(Configuration clientConfiguration) {
        if (clientConfiguration.containsKey("uri")) {
            return createMongoClientFromURI(clientConfiguration);
        } else {
            return createMongoClient(clientConfiguration);
        }
    }

    @Override
    protected MongoDatabase doCreateDatabase(MongoClient client, String dbName) {
        return client.getDatabase(dbName);
    }

    @Override
    protected void doClose(MongoClient client) {
        client.close();
    }

    private MongoClient createMongoClientFromURI(Configuration clientConfiguration) {
        String uri = clientConfiguration.getString("uri");

        if (uri == null || uri.isEmpty()) {
            throw SeedException.createNew(MongoDbErrorCodes.MISSING_URI);
        }

        MongoClientURI mongoClientURI;
        MongoClientOptions mongoClientOptions = buildMongoClientOptions(clientConfiguration);
        if (mongoClientOptions != null) {
            mongoClientURI = new MongoClientURI(uri, MongoClientOptions.builder(mongoClientOptions));
        } else {
            mongoClientURI = new MongoClientURI(uri);
        }

        return new MongoClient(mongoClientURI);
    }

    private MongoClient createMongoClient(Configuration clientConfiguration) {
        List<ServerAddress> serverAddresses = buildServerAddresses(clientConfiguration.getStringArray("hosts"));

        if (serverAddresses.isEmpty()) {
            throw SeedException.createNew(MongoDbErrorCodes.MISSING_HOSTS_CONFIGURATION);
        }

        List<MongoCredential> mongoCredentials = buildMongoCredentials(clientConfiguration.getStringArray("credentials"));

        MongoClientOptions mongoClientOptions = buildMongoClientOptions(clientConfiguration);

        if (mongoClientOptions == null) {
            if (mongoCredentials.isEmpty()) {
                if (serverAddresses.size() == 1) {
                    return new MongoClient(serverAddresses.get(0));
                } else {
                    return new MongoClient(serverAddresses);
                }
            } else {
                if (serverAddresses.size() == 1) {
                    return new MongoClient(serverAddresses.get(0), mongoCredentials);
                } else {
                    return new MongoClient(serverAddresses, mongoCredentials);
                }
            }
        } else {
            if (mongoCredentials.isEmpty()) {
                if (serverAddresses.size() == 1) {
                    return new MongoClient(serverAddresses.get(0), mongoClientOptions);
                } else {
                    return new MongoClient(serverAddresses, mongoClientOptions);
                }
            } else {
                if (serverAddresses.size() == 1) {
                    return new MongoClient(serverAddresses.get(0), mongoCredentials, mongoClientOptions);
                } else {
                    return new MongoClient(serverAddresses, mongoCredentials, mongoClientOptions);
                }
            }
        }
    }

    private MongoClientOptions buildMongoClientOptions(Configuration clientConfiguration) {
        Iterator<String> it = clientConfiguration.getKeys("option");
        MongoClientOptions.Builder builder = MongoClientOptions.builder();

        while (it.hasNext()) {
            String key = it.next();
            String option = key.substring(7);

            if ("description".equals(option)) {
                builder.description(clientConfiguration.getString(key));
            } else if ("minConnectionsPerHost".equals(option)) {
                builder.minConnectionsPerHost(clientConfiguration.getInt(key));
            } else if ("connectionsPerHost".equals(option)) {
                builder.connectionsPerHost(clientConfiguration.getInt(key));
            } else if ("threadsAllowedToBlockForConnectionMultiplier".equals(option)) {
                builder.threadsAllowedToBlockForConnectionMultiplier(clientConfiguration.getInt(key));
            } else if ("serverSelectionTimeout".equals(option)) {
                builder.serverSelectionTimeout(clientConfiguration.getInt(key));
            } else if ("maxWaitTime".equals(option)) {
                builder.maxWaitTime(clientConfiguration.getInt(key));
            } else if ("maxConnectionIdleTime".equals(option)) {
                builder.maxConnectionIdleTime(clientConfiguration.getInt(key));
            } else if ("maxConnectionLifeTime".equals(option)) {
                builder.maxConnectionLifeTime(clientConfiguration.getInt(key));
            } else if ("connectTimeout".equals(option)) {
                builder.connectTimeout(clientConfiguration.getInt(key));
            } else if ("socketTimeout".equals(option)) {
                builder.socketTimeout(clientConfiguration.getInt(key));
            } else if ("socketKeepAlive".equals(option)) {
                builder.socketKeepAlive(clientConfiguration.getBoolean(key));
            } else if ("sslEnabled".equals(option)) {
                builder.sslEnabled(clientConfiguration.getBoolean(key));
            } else if ("sslInvalidHostNameAllowed".equals(option)) {
                builder.sslInvalidHostNameAllowed(clientConfiguration.getBoolean(key));
            } else if ("readPreference".equals(option)) {
                builder.readPreference(ReadPreference.valueOf(clientConfiguration.getString(key)));
            } else if ("writeConcern".equals(option)) {
                builder.writeConcern(WriteConcern.valueOf(clientConfiguration.getString(key)));
            } else if ("codecRegistry".equals(option)) {
                builder.codecRegistry(instanceFromClassName(CodecRegistry.class, clientConfiguration.getString(key)));
            } else if ("socketFactory".equals(option)) {
                builder.socketFactory(instanceFromClassName(SocketFactory.class, clientConfiguration.getString(key)));
            } else if ("cursorFinalizerEnabled".equals(option)) {
                builder.cursorFinalizerEnabled(clientConfiguration.getBoolean(key));
            } else if ("alwaysUseMBeans".equals(option)) {
                builder.alwaysUseMBeans(clientConfiguration.getBoolean(key));
            } else if ("dbDecoderFactory".equals(option)) {
                builder.dbDecoderFactory(instanceFromClassName(DBDecoderFactory.class, clientConfiguration.getString(key)));
            } else if ("dbEncoderFactory".equals(option)) {
                builder.dbEncoderFactory(instanceFromClassName(DBEncoderFactory.class, clientConfiguration.getString(key)));
            } else if ("heartbeatFrequency".equals(option)) {
                builder.heartbeatFrequency(clientConfiguration.getInt(key));
            } else if ("minHeartbeatFrequency".equals(option)) {
                builder.minHeartbeatFrequency(clientConfiguration.getInt(key));
            } else if ("heartbeatConnectTimeout".equals(option)) {
                builder.heartbeatConnectTimeout(clientConfiguration.getInt(key));
            } else if ("heartbeatSocketTimeout".equals(option)) {
                builder.heartbeatSocketTimeout(clientConfiguration.getInt(key));
            } else {
                throw SeedException.createNew(MongoDbErrorCodes.UNKNOWN_CLIENT_OPTION).put("option", option);
            }
        }

        return builder.build();
    }
}
