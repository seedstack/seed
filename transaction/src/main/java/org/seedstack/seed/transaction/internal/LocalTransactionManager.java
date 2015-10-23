/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.transaction.internal;

import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.transaction.api.Propagation;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This transaction manager implements local transactions behavior, i.e. transactions that cannot span on multiple
 * resources.
 *
 * @author adrien.lauer@mpsa.com
 */
public class LocalTransactionManager extends AbstractTransactionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalTransactionManager.class);

    @Override
    protected Object doMethodInterception(String logPrefix, MethodInvocation invocation, TransactionMetadata transactionMetadata, TransactionHandler<Object> transactionHandler) throws Throwable {
        Object currentTransaction = transactionHandler.getCurrentTransaction();
        PropagationResult propagationResult = handlePropagation(transactionMetadata.getPropagation(), currentTransaction);

        if (propagationResult.isNewTransactionNeeded()) {
            LOGGER.debug("{}: initializing transaction handler", logPrefix);
            transactionHandler.doInitialize(transactionMetadata);
        }

        Object result = null;
        try {
            if (propagationResult.isNewTransactionNeeded()) {
                LOGGER.debug("{}: creating a new transaction", logPrefix);
                currentTransaction = transactionHandler.doCreateTransaction();
            } else {
                LOGGER.debug("{}: participating in an existing transaction", logPrefix);
            }

            try {
                if (propagationResult.isNewTransactionNeeded()) {
                    LOGGER.debug("{}: beginning the transaction", logPrefix);
                    transactionHandler.doBeginTransaction(currentTransaction);
                }

                try {
                    try {
                        LOGGER.debug("{}: invocation started", logPrefix);
                        result = invocation.proceed();
                        LOGGER.debug("{}: invocation ended", logPrefix);
                    } catch (Exception exception) {
                        doHandleException(logPrefix, exception, transactionMetadata, currentTransaction);
                    }
                } catch (Throwable throwable) {
                    if (propagationResult.isNewTransactionNeeded()) {
                        LOGGER.debug("{}: rolling back the transaction after invocation exception", logPrefix);
                        transactionHandler.doRollbackTransaction(currentTransaction);
                    } else if (transactionMetadata.isRollbackOnParticipationFailure()) {
                        LOGGER.debug("{}: marking the transaction as rollback-only after invocation exception", logPrefix);
                        transactionHandler.doMarkTransactionAsRollbackOnly(currentTransaction);
                    }

                    throw throwable;
                }

                if (propagationResult.isNewTransactionNeeded()) {
                    LOGGER.debug("{}: committing transaction", logPrefix);
                    transactionHandler.doCommitTransaction(currentTransaction);
                }
            } finally {
                if (propagationResult.isNewTransactionNeeded()) {
                    LOGGER.debug("{}: releasing transaction", logPrefix);
                    transactionHandler.doReleaseTransaction(currentTransaction);
                }
            }
        } finally {
            if (propagationResult.isNewTransactionNeeded()) {
                LOGGER.debug("{}: cleaning up transaction handler", logPrefix);
                transactionHandler.doCleanup();
            }
        }

        return result;
    }

    private PropagationResult handlePropagation(Propagation propagation, Object currentTransaction) {
        switch (propagation) {
            case MANDATORY:
                if (currentTransaction == null) {
                    throw SeedException.createNew(TransactionErrorCode.TRANSACTION_NEEDED_WHEN_USING_PROPAGATION_MANDATORY);
                }

                return new PropagationResult(false);
            case NEVER:
                if (currentTransaction != null) {
                    throw SeedException.createNew(TransactionErrorCode.NO_TRANSACTION_ALLOWED_WHEN_USING_PROPAGATION_NEVER);
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
                throw SeedException.createNew(TransactionErrorCode.PROPAGATION_NOT_SUPPORTED).put("propagation", propagation);
        }
    }
}