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

import com.google.inject.PrivateModule;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import org.seedstack.seed.persistence.redis.api.RedisExceptionHandler;
import org.seedstack.seed.transaction.utils.TransactionalClassProxy;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

import java.util.Map;

class RedisModule extends PrivateModule {
    private final Map<String, Class<? extends RedisExceptionHandler>> exceptionHandlerClasses;
    private final Map<String, JedisPool> jediPools;

    public RedisModule(Map<String, JedisPool> jedisPools, Map<String, Class<? extends RedisExceptionHandler>> exceptionHandlerClasses) {
        this.jediPools = jedisPools;
        this.exceptionHandlerClasses = exceptionHandlerClasses;
    }

    @Override
    protected void configure() {
        RedisLink<Transaction> transactionRedisLink = new RedisLink<Transaction>();
        bind(Transaction.class).toInstance(TransactionalClassProxy.create(Transaction.class, transactionRedisLink));

        RedisLink<Pipeline> pipelineRedisLink = new RedisLink<Pipeline>();
        bind(Pipeline.class).toInstance(TransactionalClassProxy.create(Pipeline.class, pipelineRedisLink));

        for (Map.Entry<String, JedisPool> jedisPoolEntry : jediPools.entrySet()) {
            bindClient(jedisPoolEntry.getKey(), jedisPoolEntry.getValue(), transactionRedisLink, pipelineRedisLink);

            bind(JedisPool.class).annotatedWith(Names.named(jedisPoolEntry.getKey())).toInstance(jedisPoolEntry.getValue());
            expose(JedisPool.class).annotatedWith(Names.named(jedisPoolEntry.getKey()));
        }

        expose(Transaction.class);
        expose(Pipeline.class);
    }

    private void bindClient(String name, JedisPool jedisPool, RedisLink<Transaction> transactionRedisLink, RedisLink<Pipeline> pipelineRedisLink) {
        Class<? extends RedisExceptionHandler> exceptionHandlerClass = exceptionHandlerClasses.get(name);

        if (exceptionHandlerClass != null) {
            bind(RedisExceptionHandler.class).annotatedWith(Names.named(name)).to(exceptionHandlerClass);
        } else {
            bind(RedisExceptionHandler.class).annotatedWith(Names.named(name)).toProvider(Providers.<RedisExceptionHandler>of(null));
        }

        RedisTransactionHandler redisTransactionHandler = new RedisTransactionHandler(transactionRedisLink, jedisPool);
        bind(RedisTransactionHandler.class).annotatedWith(Names.named(name)).toInstance(redisTransactionHandler);

        RedisPipelinedTransactionHandler redisPipelinedTransactionHandler = new RedisPipelinedTransactionHandler(pipelineRedisLink, jedisPool);
        bind(RedisPipelinedTransactionHandler.class).annotatedWith(Names.named(name)).toInstance(redisPipelinedTransactionHandler);

        expose(RedisExceptionHandler.class).annotatedWith(Names.named(name));
        expose(RedisTransactionHandler.class).annotatedWith(Names.named(name));
        expose(RedisPipelinedTransactionHandler.class).annotatedWith(Names.named(name));
    }
}
