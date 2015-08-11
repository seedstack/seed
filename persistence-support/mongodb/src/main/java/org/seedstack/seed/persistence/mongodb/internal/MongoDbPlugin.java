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
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.persistence.mongodb.api.MongoDbErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MongoDbPlugin extends AbstractPlugin {
    public static final String MONGO_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.seed.persistence.mongodb";

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbPlugin.class);

    private static class SyncHolder {
        private static final MongoDbManager INSTANCE = new SyncMongoDbManager();
    }

    private static class AsyncHolder {
        private static final MongoDbManager INSTANCE = new AsyncMongoDbManager();
    }

    private boolean hasSyncClients = false;
    private boolean hasAsyncClients = false;

    @Override
    public String name() {
        return "seed-persistence-mongodb-plugin";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InitState init(InitContext initContext) {
        Application application = null;
        Configuration mongoConfiguration = null;

        for (Plugin plugin : initContext.pluginsRequired()) {
            if (plugin instanceof ApplicationPlugin) {
                application = ((ApplicationPlugin) plugin).getApplication();
                mongoConfiguration = application.getConfiguration().subset(MongoDbPlugin.MONGO_PLUGIN_CONFIGURATION_PREFIX);
            }
        }

        if (application == null) {
            throw new PluginException("Unable to find application plugin");
        }

        String[] clientNames = mongoConfiguration.getStringArray("clients");
        Set<String> allDbNames = new HashSet<String>();

        if (clientNames == null || clientNames.length == 0) {
            LOGGER.info("No Mongo client configured, MongoDB support disabled");
            return InitState.INITIALIZED;
        }

        for (String clientName : clientNames) {
            Configuration clientConfiguration = mongoConfiguration.subset("client." + clientName);
            boolean async = clientConfiguration.getBoolean("async", false);

            if (async) {
                AsyncHolder.INSTANCE.registerClient(clientName, clientConfiguration);
                hasAsyncClients = true;
            } else {
                SyncHolder.INSTANCE.registerClient(clientName, clientConfiguration);
                hasSyncClients = true;
            }

            String[] dbNames = clientConfiguration.getStringArray("databases");

            if (dbNames != null) {
                for (String dbName : dbNames) {
                    String alias = clientConfiguration.getString(String.format("alias.%s", dbName), dbName);

                    if (allDbNames.contains(alias)) {
                        throw SeedException.createNew(MongoDbErrorCodes.DUPLICATE_DATABASE_NAME)
                                .put("clientName", clientName)
                                .put("dbName", dbName);
                    } else {
                        allDbNames.add(alias);
                    }

                    if (async) {
                        AsyncHolder.INSTANCE.registerDatabase(clientName, dbName, alias);
                    } else {
                        SyncHolder.INSTANCE.registerDatabase(clientName, dbName, alias);
                    }
                }
            }
        }

        return InitState.INITIALIZED;
    }

    @Override
    public void stop() {
        if (hasSyncClients) {
            SyncHolder.INSTANCE.shutdown();
        }

        if (hasAsyncClients) {
            AsyncHolder.INSTANCE.shutdown();
        }
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        return plugins;
    }

    @Override
    public Object nativeUnitModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                if (hasSyncClients) {
                    install(SyncHolder.INSTANCE.getModule());
                }

                if (hasAsyncClients) {
                    install(AsyncHolder.INSTANCE.getModule());
                }
            }
        };
    }
}
