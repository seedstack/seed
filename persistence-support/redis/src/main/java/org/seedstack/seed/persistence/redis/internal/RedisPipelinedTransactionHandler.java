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

import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.persistence.redis.api.RedisErrorCodes;
import org.seedstack.seed.transaction.spi.TransactionMetadata;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.io.IOException;


class RedisPipelinedTransactionHandler implements org.seedstack.seed.transaction.spi.TransactionHandler<Pipeline> {
    private final RedisLink<Pipeline> redisLink;
    private final JedisPool jedisPool;

    RedisPipelinedTransactionHandler(RedisLink<Pipeline> redisLink, JedisPool jedisPool) {
        this.redisLink = redisLink;
        this.jedisPool = jedisPool;
    }

    @Override
    public void doInitialize(TransactionMetadata transactionMetadata) {
        this.redisLink.push(this.jedisPool.getResource());
    }

    @Override
    public Pipeline doCreateTransaction() {
        RedisLink<Pipeline>.Holder holder = this.redisLink.getHolder();
        holder.attached = holder.jedis.pipelined();
        holder.attached.multi();
        return holder.attached;
    }

    @Override
    public void doJoinGlobalTransaction() {
        // not supported
    }

    @Override
    public void doBeginTransaction(Pipeline currentTransaction) {
        // nothing to do (transaction already began)
    }

    @Override
    public void doCommitTransaction(Pipeline currentTransaction) {
        currentTransaction.exec();
    }

    @Override
    public void doMarkTransactionAsRollbackOnly(Pipeline currentTransaction) {
        // not supported
    }

    @Override
    public void doRollbackTransaction(Pipeline currentTransaction) {
        currentTransaction.clear();
    }

    @Override
    public void doReleaseTransaction(Pipeline currentTransaction) {
        try {
            currentTransaction.close();
        } catch (IOException e) {
            throw SeedException.wrap(e, RedisErrorCodes.UNABLE_TO_CLOSE_TRANSACTION);
        }
    }

    @Override
    public void doCleanup() {
        this.redisLink.pop().close();
    }

    @Override
    public Pipeline getCurrentTransaction() {
        RedisLink<Pipeline>.Holder holder = this.redisLink.getHolder();

        if (holder == null) {
            return null;
        } else {
            return holder.attached;
        }
    }
}
