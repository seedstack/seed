/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.redis.internal;

import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.persistence.redis.api.RedisErrorCodes;
import org.seedstack.seed.persistence.redis.api.RedisExceptionHandler;
import org.seedstack.seed.transaction.internal.TransactionPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RedisPlugin extends AbstractPlugin {
    public static final String REDIS_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.seed.persistence.redis";

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisPlugin.class);

    private final Map<String, JedisPool> jedisPools = new HashMap<String, JedisPool>();
    private final Map<String, Class<? extends RedisExceptionHandler>> exceptionHandlerClasses = new HashMap<String, Class<? extends RedisExceptionHandler>>();

    @Override
    public String name() {
        return "seed-persistence-redis-plugin";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InitState init(InitContext initContext) {
        Application application = null;
        TransactionPlugin transactionPlugin = null;
        Configuration redisConfiguration = null;

        for (Plugin plugin : initContext.pluginsRequired()) {
            if (plugin instanceof ApplicationPlugin) {
                application = ((ApplicationPlugin) plugin).getApplication();
                redisConfiguration = application.getConfiguration().subset(RedisPlugin.REDIS_PLUGIN_CONFIGURATION_PREFIX);
            } else if (plugin instanceof TransactionPlugin) {
                transactionPlugin = (TransactionPlugin) plugin;
            }
        }

        if (application == null) {
            throw new PluginException("Unable to find application plugin");
        }
        if (transactionPlugin == null) {
            throw new PluginException("Unable to find transaction plugin");
        }

        String[] clients = redisConfiguration.getStringArray("clients");

        if (clients == null || clients.length == 0) {
            LOGGER.info("No Redis client configured, Redis support disabled");
            return InitState.INITIALIZED;
        }

        for (String client : clients) {
            Configuration clientConfiguration = redisConfiguration.subset("client." + client);

            String exceptionHandler = clientConfiguration.getString("exception-handler");
            if (exceptionHandler != null && !exceptionHandler.isEmpty()) {
                try {
                    exceptionHandlerClasses.put(client, (Class<? extends RedisExceptionHandler>) Class.forName(exceptionHandler));
                } catch (Exception e) {
                    throw SeedException.wrap(e, RedisErrorCodes.UNABLE_TO_LOAD_EXCEPTION_HANDLER_CLASS).put("clientName", client).put("exceptionHandlerClass", exceptionHandler);
                }
            }

            try {
                jedisPools.put(client, createJedisPool(clientConfiguration));
            } catch (Exception e) {
                throw SeedException.wrap(e, RedisErrorCodes.UNABLE_TO_CREATE_CLIENT).put("clientName", client);
            }
        }

        if (clients.length == 1) {
            RedisTransactionMetadataResolver.defaultClient = clients[0];
        }

        transactionPlugin.registerTransactionHandler(RedisTransactionHandler.class);
        transactionPlugin.registerTransactionHandler(RedisPipelinedTransactionHandler.class);

        return InitState.INITIALIZED;
    }

    @Override
    public void stop() {
        for (Map.Entry<String, JedisPool> jedisPoolEntry : jedisPools.entrySet()) {
            LOGGER.info("Shutting down {} Jedis pool", jedisPoolEntry.getKey());
            try {
                jedisPoolEntry.getValue().close();
            } catch (Exception e) {
                LOGGER.error(String.format("Unable to properly close %s Jedi pool", jedisPoolEntry.getKey()), e);
            }
        }
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().descendentTypeOf(RedisExceptionHandler.class).build();
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        plugins.add(TransactionPlugin.class);
        return plugins;
    }

    @Override
    public Object nativeUnitModule() {
        return new RedisModule(jedisPools, exceptionHandlerClasses);
    }

    private JedisPool createJedisPool(Configuration clientConfiguration) {
        String url = clientConfiguration.getString("url");

        if (url == null || url.isEmpty()) {
            throw SeedException.createNew(RedisErrorCodes.MISSING_URL_CONFIGURATION);
        }

        return new JedisPool(new JedisPoolConfig(), url);
    }
}
