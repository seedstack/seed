/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.jms.internal;

import javax.jms.JMSException;
import javax.jms.Session;

import org.seedstack.seed.transaction.spi.TransactionHandler;

abstract class AbstractJmsTransactionHandler implements TransactionHandler<Session> {
    @Override
    public void doJoinGlobalTransaction() {
        // nothing to do (with a JNDI provided connection factory JTA is automatically enabled)
    }

    @Override
    public void doBeginTransaction(Session session) {
        // nothing to do
    }

    @Override
    public void doCommitTransaction(Session session) {
        try {
            session.commit();
        } catch (JMSException e) {
            throw new IllegalStateException("Unable to commit JMS transaction ", e);
        }
    }

    @Override
    public void doMarkTransactionAsRollbackOnly(Session currentTransaction) {
        // nothing to do
    }

    @Override
    public void doRollbackTransaction(Session session) {
        try {
            session.rollback();
        } catch (JMSException e) {
            throw new IllegalStateException("Unable to rollback JMS transaction ", e);
        }
    }

    @Override
    public void doReleaseTransaction(Session session) {
        // nothing to do
    }
}
