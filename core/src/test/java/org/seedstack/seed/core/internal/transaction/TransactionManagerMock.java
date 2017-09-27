/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.transaction;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

public class TransactionManagerMock implements TransactionManager {
    private UserTransactionMock userTransaction;
    private int suspendCallCount = 0;
    private int resumeCallCount = 0;

    public TransactionManagerMock(UserTransactionMock userTransaction) {
        this.userTransaction = userTransaction;
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        userTransaction.begin();
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
            SecurityException, IllegalStateException, SystemException {
        userTransaction.commit();
    }

    @Override
    public int getStatus() throws SystemException {
        return userTransaction.getStatus();
    }

    @Override
    public Transaction getTransaction() throws SystemException {
        return new TransactionMock(userTransaction);
    }

    @Override
    public void resume(Transaction tobj) throws InvalidTransactionException, IllegalStateException, SystemException {
        this.userTransaction.setStatus(tobj.getStatus());
        this.resumeCallCount++;
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        userTransaction.rollback();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        userTransaction.setRollbackOnly();
    }

    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {
        userTransaction.setTransactionTimeout(seconds);
    }

    @Override
    public Transaction suspend() throws SystemException {
        TransactionMock suspendedTransaction = new TransactionMock(new UserTransactionMock(userTransaction));
        userTransaction.setStatus(Status.STATUS_NO_TRANSACTION);
        suspendCallCount++;
        return suspendedTransaction;
    }

    int getSuspendCallCount() {
        return suspendCallCount;
    }

    int getResumeCallCount() {
        return resumeCallCount;
    }
}
