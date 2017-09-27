/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.transaction;

import org.aopalliance.intercept.MethodInvocation;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.transaction.Propagation;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;

/**
 * This transaction manager implements local transactions behavior, i.e. transactions that cannot span on multiple
 * resources.
 */
public class LocalTransactionManager extends AbstractTransactionManager {
    @Override
    protected Object doMethodInterception(TransactionLogger transactionLogger, MethodInvocation invocation,
            TransactionMetadata transactionMetadata, TransactionHandler<Object> transactionHandler) throws Throwable {
        Object currentTransaction = transactionHandler.getCurrentTransaction();
        PropagationResult propagationResult = handlePropagation(transactionMetadata.getPropagation(),
                currentTransaction);

        if (propagationResult.isNewTransactionNeeded()) {
            transactionLogger.log("initializing transaction handler");
            transactionHandler.doInitialize(transactionMetadata);
        }

        Object result;
        try {
            if (propagationResult.isNewTransactionNeeded()) {
                transactionLogger.log("creating a new transaction");
                currentTransaction = transactionHandler.doCreateTransaction();
            } else {
                transactionLogger.log("participating in an existing transaction");
            }

            try {
                if (propagationResult.isNewTransactionNeeded()) {
                    transactionLogger.log("beginning the transaction");
                    transactionHandler.doBeginTransaction(currentTransaction);
                }

                try {
                    result = doInvocation(transactionLogger, invocation, transactionMetadata, currentTransaction);
                } catch (Throwable throwable) {
                    if (propagationResult.isNewTransactionNeeded()) {
                        transactionLogger.log("rolling back the transaction after invocation exception");
                        transactionHandler.doRollbackTransaction(currentTransaction);
                    } else if (currentTransaction != null && transactionMetadata.isRollbackOnParticipationFailure()) {
                        transactionLogger.log("marking the transaction as rollback-only after invocation exception");
                        transactionHandler.doMarkTransactionAsRollbackOnly(currentTransaction);
                    }

                    throw throwable;
                }

                if (propagationResult.isNewTransactionNeeded()) {
                    transactionLogger.log("committing transaction");
                    transactionHandler.doCommitTransaction(currentTransaction);
                }
            } finally {
                if (propagationResult.isNewTransactionNeeded()) {
                    transactionLogger.log("releasing transaction");
                    transactionHandler.doReleaseTransaction(currentTransaction);
                }
            }
        } finally {
            if (propagationResult.isNewTransactionNeeded()) {
                transactionLogger.log("cleaning up transaction handler");
                transactionHandler.doCleanup();
            }
        }

        return result;
    }

    private PropagationResult handlePropagation(Propagation propagation, Object currentTransaction) {
        switch (propagation) {
            case MANDATORY:
                if (currentTransaction == null) {
                    throw SeedException.createNew(
                            TransactionErrorCode.TRANSACTION_NEEDED_WHEN_USING_PROPAGATION_MANDATORY);
                }

                return new PropagationResult(false);
            case NEVER:
                if (currentTransaction != null) {
                    throw SeedException.createNew(
                            TransactionErrorCode.NO_TRANSACTION_ALLOWED_WHEN_USING_PROPAGATION_NEVER);
                }

                return new PropagationResult(false);
            case NOT_SUPPORTED:
                if (currentTransaction != null) {
                    throw SeedException.createNew(TransactionErrorCode.TRANSACTION_SUSPENSION_IS_NOT_SUPPORTED);
                }

                return new PropagationResult(false);
            case REQUIRED:
                return new PropagationResult(currentTransaction == null);
            case REQUIRES_NEW:
                return new PropagationResult(true);
            case SUPPORTS:
                return new PropagationResult(false);
            default:
                throw SeedException.createNew(TransactionErrorCode.PROPAGATION_NOT_SUPPORTED).put("propagation",
                        propagation);
        }
    }
}