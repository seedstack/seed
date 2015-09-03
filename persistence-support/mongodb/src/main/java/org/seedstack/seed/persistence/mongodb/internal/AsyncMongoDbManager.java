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

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.connection.ClusterConnectionMode;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.connection.ClusterType;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.connection.ServerSettings;
import com.mongodb.connection.SocketSettings;
import com.mongodb.connection.SslSettings;
import com.mongodb.selector.ServerSelector;
import org.apache.commons.configuration.Configuration;
import org.bson.codecs.configuration.CodecRegistry;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.persistence.mongodb.api.MongoDbErrorCodes;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

class AsyncMongoDbManager extends AbstractMongoDbManager<MongoClient, MongoDatabase> {
    @Override
    protected MongoClient doCreateClient(Configuration clientConfiguration) {
        String uri = clientConfiguration.getString("uri");
        if (uri != null && !uri.isEmpty()) {
            return MongoClients.create(uri);
        } else {
            return MongoClients.create(buildMongoClientSettings(clientConfiguration));
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

    private MongoClientSettings buildMongoClientSettings(Configuration clientConfiguration) {
        MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder();
        ClusterSettings.Builder clusterSettingsBuilder = ClusterSettings.builder();
        SocketSettings.Builder socketSettingsBuilder = SocketSettings.builder();
        SocketSettings.Builder heartbeatSocketSettingsBuilder = SocketSettings.builder();
        ConnectionPoolSettings.Builder connectionSettingsBuilder = ConnectionPoolSettings.builder();
        ServerSettings.Builder serverSettingsBuilder = ServerSettings.builder();
        SslSettings.Builder sslSettingsBuilder = SslSettings.builder();

        // Apply hosts
        String[] hosts = clientConfiguration.getStringArray("hosts");
        if (hosts != null && hosts.length > 0) {
            clusterSettingsBuilder.hosts(buildServerAddresses(hosts));
        }

        // Apply credentials
        settingsBuilder.credentialList(buildMongoCredentials(clientConfiguration.getStringArray("credentials")));

        // Apply configuration properties
        Iterator<String> it = clientConfiguration.getKeys("setting");
        while (it.hasNext()) {
            String key = it.next();
            String setting = key.substring(8);

            // cluster settings
            if ("cluster.description".equals(setting)) {
                clusterSettingsBuilder.description(clientConfiguration.getString(key));
            } else if ("cluster.mode".equals(setting)) {
                clusterSettingsBuilder.mode(ClusterConnectionMode.valueOf(clientConfiguration.getString(key)));
            } else if ("cluster.requiredReplicaSetName".equals(setting)) {
                clusterSettingsBuilder.requiredReplicaSetName(clientConfiguration.getString(key));
            } else if ("cluster.requiredClusterType".equals(setting)) {
                clusterSettingsBuilder.requiredClusterType(ClusterType.valueOf(clientConfiguration.getString(key)));
            } else if ("cluster.serverSelector".equals(setting)) {
                clusterSettingsBuilder.serverSelector(instanceFromClassName(ServerSelector.class, clientConfiguration.getString(key)));
            } else if ("cluster.serverSelectionTimeout".equals(setting)) {
                clusterSettingsBuilder.serverSelectionTimeout(clientConfiguration.getLong(key), TimeUnit.MILLISECONDS);
            } else if ("cluster.maxWaitQueueSize".equals(setting)) {
                clusterSettingsBuilder.maxWaitQueueSize(clientConfiguration.getInt(key));
            }

            // socket settings
            else if ("socket.connectTimeout".equals(setting)) {
                socketSettingsBuilder.connectTimeout(clientConfiguration.getInt(key), TimeUnit.MILLISECONDS);
            } else if ("socket.readTimeout".equals(setting)) {
                socketSettingsBuilder.readTimeout(clientConfiguration.getInt(key), TimeUnit.MILLISECONDS);
            } else if ("socket.keepAlive".equals(setting)) {
                socketSettingsBuilder.keepAlive(clientConfiguration.getBoolean(key));
            } else if ("socket.receiveBufferSize".equals(setting)) {
                socketSettingsBuilder.receiveBufferSize(clientConfiguration.getInt(key));
            } else if ("socket.sendBufferSize".equals(setting)) {
                socketSettingsBuilder.sendBufferSize(clientConfiguration.getInt(key));
            }

            // heartbeat socket settings
            else if ("heartbeatSocket.connectTimeout".equals(setting)) {
                heartbeatSocketSettingsBuilder.connectTimeout(clientConfiguration.getInt(key), TimeUnit.MILLISECONDS);
            } else if ("heartbeatSocket.readTimeout".equals(setting)) {
                heartbeatSocketSettingsBuilder.readTimeout(clientConfiguration.getInt(key), TimeUnit.MILLISECONDS);
            } else if ("heartbeatSocket.keepAlive".equals(setting)) {
                heartbeatSocketSettingsBuilder.keepAlive(clientConfiguration.getBoolean(key));
            } else if ("heartbeatSocket.receiveBufferSize".equals(setting)) {
                heartbeatSocketSettingsBuilder.receiveBufferSize(clientConfiguration.getInt(key));
            } else if ("heartbeatSocket.sendBufferSize".equals(setting)) {
                heartbeatSocketSettingsBuilder.sendBufferSize(clientConfiguration.getInt(key));
            }

            // connection pool settings
            else if ("connectionPool.maxSize".equals(setting)) {
                connectionSettingsBuilder.maxSize(clientConfiguration.getInt(key));
            } else if ("connectionPool.minSize".equals(setting)) {
                connectionSettingsBuilder.minSize(clientConfiguration.getInt(key));
            } else if ("connectionPool.maxWaitQueueSize".equals(setting)) {
                connectionSettingsBuilder.maxWaitQueueSize(clientConfiguration.getInt(key));
            } else if ("connectionPool.maxWaitTime".equals(setting)) {
                connectionSettingsBuilder.maxWaitTime(clientConfiguration.getLong(key), TimeUnit.MILLISECONDS);
            } else if ("connectionPool.maxConnectionLifeTime".equals(setting)) {
                connectionSettingsBuilder.maxConnectionLifeTime(clientConfiguration.getLong(key), TimeUnit.MILLISECONDS);
            } else if ("connectionPool.maxConnectionIdleTime".equals(setting)) {
                connectionSettingsBuilder.maxConnectionIdleTime(clientConfiguration.getLong(key), TimeUnit.MILLISECONDS);
            } else if ("connectionPool.maintenanceInitialDelay".equals(setting)) {
                connectionSettingsBuilder.maintenanceInitialDelay(clientConfiguration.getLong(key), TimeUnit.MILLISECONDS);
            } else if ("connectionPool.maintenanceFrequency".equals(setting)) {
                connectionSettingsBuilder.maintenanceFrequency(clientConfiguration.getLong(key), TimeUnit.MILLISECONDS);
            }

            // server settings
            else if ("server.heartbeatFrequency".equals(setting)) {
                serverSettingsBuilder.heartbeatFrequency(clientConfiguration.getLong(key), TimeUnit.MILLISECONDS);
            } else if ("server.minHeartbeatFrequency".equals(setting)) {
                serverSettingsBuilder.minHeartbeatFrequency(clientConfiguration.getLong(key), TimeUnit.MILLISECONDS);
            }

            // ssl settings
            else if ("ssl.enabled".equals(setting)) {
                sslSettingsBuilder.enabled(clientConfiguration.getBoolean(key));
            } else if ("ssl.invalidHostNameAllowed".equals(setting)) {
                sslSettingsBuilder.invalidHostNameAllowed(clientConfiguration.getBoolean(key));
            }

            // global settings
            else if ("readPreference".equals(setting)) {
                settingsBuilder.readPreference(ReadPreference.valueOf(clientConfiguration.getString(key)));
            } else if ("writeConcern".equals(setting)) {
                settingsBuilder.writeConcern(WriteConcern.valueOf(clientConfiguration.getString(key)));
            } else if ("codecRegistry".equals(setting)) {
                settingsBuilder.codecRegistry(instanceFromClassName(CodecRegistry.class, clientConfiguration.getString(key)));
            } else {
                throw SeedException.createNew(MongoDbErrorCodes.UNKNOWN_CLIENT_SETTING).put("setting", setting);
            }
        }

        settingsBuilder.clusterSettings(clusterSettingsBuilder.build());
        settingsBuilder.socketSettings(socketSettingsBuilder.build());
        settingsBuilder.heartbeatSocketSettings(heartbeatSocketSettingsBuilder.build());
        settingsBuilder.connectionPoolSettings(connectionSettingsBuilder.build());
        settingsBuilder.serverSettings(serverSettingsBuilder.build());
        settingsBuilder.sslSettings(sslSettingsBuilder.build());

        return settingsBuilder.build();
    }
}
