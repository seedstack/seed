/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.transaction;

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
    public void begin() {
        userTransaction.begin();
    }

    @Override
    public void commit() throws
            SecurityException, IllegalStateException {
        userTransaction.commit();
    }

    @Override
    public int getStatus() {
        return userTransaction.getStatus();
    }

    @Override
    public Transaction getTransaction() {
        return new TransactionMock(userTransaction);
    }

    @Override
    public void resume(Transaction tobj) throws IllegalStateException, SystemException {
        this.userTransaction.setStatus(tobj.getStatus());
        this.resumeCallCount++;
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException {
        userTransaction.rollback();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException {
        userTransaction.setRollbackOnly();
    }

    @Override
    public void setTransactionTimeout(int seconds) {
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
