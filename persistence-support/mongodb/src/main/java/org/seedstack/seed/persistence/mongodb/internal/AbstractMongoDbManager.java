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

import com.google.inject.Module;
import com.mongodb.AuthenticationMechanism;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.persistence.mongodb.api.MongoDbErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractMongoDbManager<C, D> implements MongoDbManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMongoDbManager.class);

    private final Map<String, C> mongoClients = new HashMap<String, C>();
    private final Map<String, D> mongoDatabases = new HashMap<String, D>();

    @Override
    public void registerClient(String clientName, Configuration clientConfiguration) {
        LOGGER.info("Creating MongoDB client {}", clientName);
        mongoClients.put(clientName, doCreateClient(clientConfiguration));
    }

    @Override
    public void registerDatabase(String clientName, String dbName, String alias) {
        C mongoClient = mongoClients.get(clientName);

        if (mongoClient == null) {
            throw SeedException.createNew(MongoDbErrorCodes.UNKNOWN_CLIENT_SPECIFIED).put("clientName", clientName).put("dbName", dbName);
        }

        mongoDatabases.put(alias, doCreateDatabase(mongoClient, dbName));
    }

    @Override
    public void shutdown() {
        try {
            for (Map.Entry<String, C> mongoClientEntry : mongoClients.entrySet()) {
                LOGGER.info("Closing MongoDB client {}", mongoClientEntry.getKey());
                try {
                    doClose(mongoClientEntry.getValue());
                } catch (Exception e) {
                    LOGGER.error(String.format("Unable to properly close MongoDB client %s", mongoClientEntry.getKey()), e);
                }
            }
        } finally {
            mongoDatabases.clear();
            mongoClients.clear();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Module getModule() {
        Type[] actualTypeArguments = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
        return new MongoDbModule<C, D>((Class<C>) actualTypeArguments[0], (Class<D>) actualTypeArguments[1], mongoClients, mongoDatabases);
    }

    protected <T> T instanceFromClassName(Class<T> clazz, String className) {
        try {
            return Class.forName(className).asSubclass(clazz).newInstance();
        } catch (Exception e) {
            throw SeedException.wrap(e, MongoDbErrorCodes.UNABLE_TO_INSTANTIATE_CLASS).put("className", className);
        }
    }

    public List<ServerAddress> buildServerAddresses(String[] addresses) {
        List<ServerAddress> serverAddresses = new ArrayList<ServerAddress>();

        if (addresses != null) {
            for (String address : addresses) {
                String[] split = address.split(":", 2);
                if (split.length == 1) {
                    serverAddresses.add(new ServerAddress(split[0]));
                } else if (split.length == 2) {
                    serverAddresses.add(new ServerAddress(split[0], Integer.parseInt(split[1])));
                } else {
                    throw SeedException.createNew(MongoDbErrorCodes.UNABLE_TO_PARSE_SERVER_ADDRESS).put("address", address);
                }
            }
        }

        return serverAddresses;
    }

    protected List<MongoCredential> buildMongoCredentials(String[] credentials) {
        List<MongoCredential> mongoCredentials = new ArrayList<MongoCredential>();

        if (credentials != null) {
            for (String credential : credentials) {
                String[] elements = credential.split(":", 3);
                if (elements.length == 3) {
                    String[] sourceElements = elements[0].split("/", 2);
                    if (sourceElements.length == 2) {
                        mongoCredentials.add(buildMongoCredential(elements[1], elements[2], sourceElements[1], sourceElements[0]));
                    } else if (sourceElements.length == 1) {
                        mongoCredentials.add(buildMongoCredential(elements[1], elements[2], sourceElements[0], null));
                    } else {
                        throw SeedException.createNew(MongoDbErrorCodes.INVALID_CREDENTIAL_SYNTAX).put("credential", credential);
                    }
                } else {
                    throw SeedException.createNew(MongoDbErrorCodes.INVALID_CREDENTIAL_SYNTAX).put("credential", credential);
                }
            }
        }

        return mongoCredentials;
    }

    protected MongoCredential buildMongoCredential(String user, String password, String source, String mechanism) {
        if (mechanism != null) {
            AuthenticationMechanism authenticationMechanism = AuthenticationMechanism.fromMechanismName(mechanism);
            switch (authenticationMechanism) {
                case PLAIN:
                    return MongoCredential.createPlainCredential(user, source, password.toCharArray());
                case MONGODB_CR:
                    return MongoCredential.createMongoCRCredential(user, source, password.toCharArray());
                case SCRAM_SHA_1:
                    return MongoCredential.createScramSha1Credential(user, source, password.toCharArray());
                case MONGODB_X509:
                    return MongoCredential.createMongoX509Credential(user);
                case GSSAPI:
                    return MongoCredential.createGSSAPICredential(user);
                default:
                    throw SeedException.createNew(MongoDbErrorCodes.UNSUPPORTED_AUTHENTICATION_MECHANISM).put("mechanism", authenticationMechanism.getMechanismName());
            }
        } else {
            return MongoCredential.createCredential(
                    user,
                    source,
                    password.toCharArray()
            );
        }
    }

    protected abstract C doCreateClient(Configuration clientConfiguration);

    protected abstract D doCreateDatabase(C client, String dbName);

    protected abstract void doClose(C client);
}
