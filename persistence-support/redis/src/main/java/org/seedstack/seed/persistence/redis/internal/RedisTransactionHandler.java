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
import redis.clients.jedis.Transaction;

import java.io.IOException;


class RedisTransactionHandler implements org.seedstack.seed.transaction.spi.TransactionHandler<Transaction> {
    private final RedisLink<Transaction> redisLink;
    private final JedisPool jedisPool;

    RedisTransactionHandler(RedisLink<Transaction> redisLink, JedisPool jedisPool) {
        this.redisLink = redisLink;
        this.jedisPool = jedisPool;
    }

    @Override
    public void doInitialize(TransactionMetadata transactionMetadata) {
        this.redisLink.push(this.jedisPool.getResource());
    }

    @Override
    public Transaction doCreateTransaction() {
        RedisLink<Transaction>.Holder holder = this.redisLink.getHolder();
        holder.attached = holder.jedis.multi();
        return holder.attached;
    }

    @Override
    public void doJoinGlobalTransaction() {
        // not supported
    }

    @Override
    public void doBeginTransaction(Transaction currentTransaction) {
        // nothing to do (transaction already began)
    }

    @Override
    public void doCommitTransaction(Transaction currentTransaction) {
        currentTransaction.exec();
    }

    @Override
    public void doMarkTransactionAsRollbackOnly(Transaction currentTransaction) {
        // not supported
    }

    @Override
    public void doRollbackTransaction(Transaction currentTransaction) {
        currentTransaction.clear();
    }

    @Override
    public void doReleaseTransaction(Transaction currentTransaction) {
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
    public Transaction getCurrentTransaction() {
        RedisLink<Transaction>.Holder holder = this.redisLink.getHolder();

        if (holder == null) {
            return null;
        } else {
            return holder.attached;
        }
    }
}
