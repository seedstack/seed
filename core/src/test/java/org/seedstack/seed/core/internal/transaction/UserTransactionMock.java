/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.transaction;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

public class UserTransactionMock implements UserTransaction {
    private int status = Status.STATUS_NO_TRANSACTION;

    public UserTransactionMock() {
    }

    public UserTransactionMock(UserTransaction userTransactionMock) throws SystemException {
        this.status = userTransactionMock.getStatus();
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        if (status != Status.STATUS_NO_TRANSACTION) {
            throw new IllegalStateException("Cannot begin transaction if there is an already existing one");
        }
        status = Status.STATUS_ACTIVE;
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
            SecurityException, IllegalStateException, SystemException {
        if (status != Status.STATUS_ACTIVE) {
            throw new IllegalStateException("Can only commit an active transaction");
        }
        status = Status.STATUS_COMMITTED;
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        if (status != Status.STATUS_ACTIVE) {
            throw new IllegalStateException("Can only rollback an active transaction");
        }
        status = Status.STATUS_ROLLEDBACK;
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        if (status != Status.STATUS_ACTIVE) {
            throw new IllegalStateException("Can only mark an active transaction as rollback only");
        }
        status = Status.STATUS_MARKED_ROLLBACK;
    }

    @Override
    public int getStatus() throws SystemException {
        return status;
    }

    void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {

    }
}
