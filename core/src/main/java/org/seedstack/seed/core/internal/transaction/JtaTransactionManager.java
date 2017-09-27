/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.transaction;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.aopalliance.intercept.MethodInvocation;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.transaction.Propagation;
import org.seedstack.seed.transaction.TransactionConfig;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;

/**
 * This transaction manager delegates to JTA the transactional behavior.
 */
public class JtaTransactionManager extends AbstractTransactionManager {
    private static final String[] AUTODETECT_TRANSACTION_MANAGER_NAMES = new String[]{"java:comp/TransactionManager",
            "java:appserver/TransactionManager", "java:pm/TransactionManager", "java:/TransactionManager"};
    protected UserTransaction userTransaction;
    protected TransactionManager transactionManager;
    @Inject
    private Context jndiContext;
    @Configuration
    private TransactionConfig.JtaConfig jtaConfig;

    @Override
    protected Object doMethodInterception(TransactionLogger transactionLogger, MethodInvocation invocation,
            TransactionMetadata transactionMetadata, TransactionHandler<Object> transactionHandler) throws Throwable {
        initJTAObjects(transactionLogger);

        PropagationResult propagationResult;
        try {
            propagationResult = handlePropagation(transactionMetadata.getPropagation());
        } catch (Exception e) {
            throw SeedException.wrap(e, TransactionErrorCode.TRANSACTION_PROPAGATION_ERROR);
        }

        Transaction suspendedTransaction = null;
        try {
            if (propagationResult.isSuspendCurrentTransaction() && userTransaction.getStatus() == Status
                    .STATUS_ACTIVE) {
                if (transactionManager != null) {
                    transactionLogger.log("suspending current JTA transaction");
                    suspendedTransaction = transactionManager.suspend();
                } else {
                    throw SeedException.createNew(TransactionErrorCode.TRANSACTION_SUSPENSION_IS_NOT_SUPPORTED);
                }
            }

            if (propagationResult.isNewTransactionNeeded()) {
                transactionLogger.log("initializing transaction handler");
                transactionHandler.doInitialize(transactionMetadata);
            }

            Object result;
            try {
                if (propagationResult.isNewTransactionNeeded()) {
                    transactionLogger.log("beginning the JTA transaction");
                    userTransaction.begin();
                } else {
                    transactionLogger.log("participating in an existing JTA transaction");
                }

                try {
                    if (userTransaction.getStatus() == Status.STATUS_ACTIVE) {
                        transactionHandler.doJoinGlobalTransaction();
                    }

                    try {
                        result = doInvocation(transactionLogger, invocation, transactionMetadata, userTransaction);
                    } catch (Throwable throwable) {
                        if (propagationResult.isNewTransactionNeeded()) {
                            transactionLogger.log("rolling back JTA transaction after invocation exception");
                            userTransaction.rollback();
                        } else if (transactionMetadata.isRollbackOnParticipationFailure()) {
                            transactionLogger.log(
                                    "marking JTA transaction as rollback-only after invocation exception");
                            userTransaction.setRollbackOnly();
                        }

                        throw throwable;
                    }

                    if (propagationResult.isNewTransactionNeeded()) {
                        transactionLogger.log("committing JTA transaction");
                        userTransaction.commit();
                    }
                } finally {
                    if (propagationResult.isNewTransactionNeeded() && userTransaction.getStatus() == Status
                            .STATUS_ACTIVE) {
                        transactionLogger.log("rolling back JTA transaction (no commit occurred)");
                        userTransaction.rollback();
                    }
                }
            } finally {
                if (propagationResult.isNewTransactionNeeded()) {
                    transactionLogger.log("cleaning up transaction handler");
                    transactionHandler.doCleanup();
                }
            }

            return result;
        } finally {
            if (suspendedTransaction != null) {
                transactionLogger.log("resuming suspended transaction");
                transactionManager.resume(suspendedTransaction);
            }
        }
    }

    protected TransactionManager getTransactionManager(TransactionLogger transactionLogger, UserTransaction ut) {
        if (ut instanceof TransactionManager) {
            transactionLogger.log("JTA UserTransaction object [{}] implements TransactionManager", ut);
            return (TransactionManager) ut;
        }

        if (jtaConfig.getTxManagerName() != null) {
            try {
                return (TransactionManager) jndiContext.lookup(jtaConfig.getTxManagerName());
            } catch (NamingException e) {
                throw SeedException.wrap(e, TransactionErrorCode.UNABLE_TO_FIND_JTA_TRANSACTION_MANAGER);
            }
        }

        for (String jndiName : AUTODETECT_TRANSACTION_MANAGER_NAMES) {
            try {
                TransactionManager tm = (TransactionManager) jndiContext.lookup(jndiName);
                transactionLogger.log("JTA TransactionManager found at JNDI location [{}]", jndiName);
                return tm;
            } catch (NamingException ex) {
                transactionLogger.log("No JTA TransactionManager found at JNDI location [{}]", jndiName, ex);
            }
        }

        return null;
    }

    protected UserTransaction getUserTransaction(TransactionLogger transactionLogger) throws NamingException {
        String jndiName = jtaConfig.getUserTxName();
        UserTransaction ut = (UserTransaction) jndiContext.lookup(jndiName);
        transactionLogger.log("JTA UserTransaction found at default JNDI location [{}]", jndiName);
        return ut;
    }

    private void initJTAObjects(TransactionLogger transactionLogger) {
        if (userTransaction == null) {
            try {
                userTransaction = getUserTransaction(transactionLogger);
            } catch (Exception e) {
                throw SeedException.wrap(e, TransactionErrorCode.UNABLE_TO_FIND_JTA_TRANSACTION);
            }
        }

        if (transactionManager == null) {
            transactionManager = getTransactionManager(transactionLogger, userTransaction);
        }
    }

    private PropagationResult handlePropagation(Propagation propagation) throws SystemException {
        switch (propagation) {
            case MANDATORY:
                if (userTransaction.getStatus() != Status.STATUS_ACTIVE) {
                    throw SeedException.createNew(
                            TransactionErrorCode.TRANSACTION_NEEDED_WHEN_USING_PROPAGATION_MANDATORY);
                }

                return new PropagationResult(false, false);
            case NEVER:
                if (userTransaction.getStatus() != Status.STATUS_NO_TRANSACTION) {
                    throw SeedException.createNew(
                            TransactionErrorCode.NO_TRANSACTION_ALLOWED_WHEN_USING_PROPAGATION_NEVER);
                }

                return new PropagationResult(false, false);
            case NOT_SUPPORTED:
                return new PropagationResult(false, true);
            case REQUIRED:
                return new PropagationResult(userTransaction.getStatus() != Status.STATUS_ACTIVE, false);
            case REQUIRES_NEW:
                return new PropagationResult(true, true);
            case SUPPORTS:
                return new PropagationResult(false, false);
            default:
                throw SeedException.createNew(TransactionErrorCode.PROPAGATION_NOT_SUPPORTED).put("propagation",
                        propagation);
        }
    }
}
