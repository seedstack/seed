/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.transaction;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAResource;

public class TransactionMock implements Transaction {
    private final UserTransaction userTransaction;

    public TransactionMock(UserTransaction userTransaction) {
        this.userTransaction = userTransaction;
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
            SecurityException, IllegalStateException, SystemException {
        userTransaction.commit();
    }

    @Override
    public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException {
        return false;
    }

    @Override
    public boolean enlistResource(XAResource xaRes) throws IllegalStateException {
        return false;
    }

    @Override
    public int getStatus() throws SystemException {
        return userTransaction.getStatus();
    }

    @Override
    public void registerSynchronization(
            Synchronization sync) throws IllegalStateException {

    }

    @Override
    public void rollback() throws IllegalStateException, SystemException {
        userTransaction.rollback();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        userTransaction.setRollbackOnly();
    }

    UserTransaction getUserTransaction() {
        return userTransaction;
    }
}
