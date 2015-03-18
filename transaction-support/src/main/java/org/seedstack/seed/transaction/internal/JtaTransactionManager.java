/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.transaction.internal;

import org.seedstack.seed.core.api.Configuration;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.CorePlugin;
import org.seedstack.seed.transaction.api.Propagation;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * This transaction manager delegates to JTA the transactional behavior.
 *
 * @author adrien.lauer@mpsa.com
 */
public class JtaTransactionManager extends AbstractTransactionManager {
    public static final String DEFAULT_USER_TRANSACTION_NAME = "java:comp/UserTransaction";
    public static final String[] FALLBACK_TRANSACTION_MANAGER_NAMES = new String[]{"java:comp/TransactionManager", "java:appserver/TransactionManager", "java:pm/TransactionManager", "java:/TransactionManager"};
    public static final String DEFAULT_TRANSACTION_SYNCHRONIZATION_REGISTRY_NAME = "java:comp/TransactionSynchronizationRegistry";
    private static final Logger LOGGER = LoggerFactory.getLogger(JtaTransactionManager.class);

    @Inject
    private Context jndiContext;

    @Configuration(value = TransactionPlugin.TRANSACTION_PLUGIN_CONFIGURATION_PREFIX + ".jta.tx-manager-name", mandatory = false)
    private String transactionManagerName;

    @Configuration(value = TransactionPlugin.TRANSACTION_PLUGIN_CONFIGURATION_PREFIX + ".jta.user-tx-name", defaultValue = DEFAULT_USER_TRANSACTION_NAME)
    private String userTransactionName;

    protected UserTransaction userTransaction;

    protected TransactionManager transactionManager;

    @Override
    protected Object doMethodInterception(String logPrefix, MethodInvocation invocation, TransactionMetadata transactionMetadata, TransactionHandler<Object> transactionHandler) throws Throwable {
        initJTAObjects(logPrefix);

        PropagationResult propagationResult;
        try {
            propagationResult = handlePropagation(transactionMetadata.getPropagation());
        } catch (Exception e) {
            throw SeedException.wrap(e, TransactionErrorCode.TRANSACTION_PROPAGATION_ERROR);
        }

        Transaction suspendedTransaction = null;
        try { // NOSONAR
            if (propagationResult.isSuspendCurrentTransaction() && userTransaction.getStatus() == Status.STATUS_ACTIVE) {
                if (transactionManager != null) {
                    LOGGER.debug("{}: suspending current JTA transaction", logPrefix);
                    suspendedTransaction = transactionManager.suspend();
                } else {
                    throw SeedException.createNew(TransactionErrorCode.TRANSACTION_SUSPENSION_IS_NOT_SUPPORTED);
                }
            }

            if (propagationResult.isNewTransactionNeeded()) {
                LOGGER.debug("{}: initializing transaction handler", logPrefix);
                transactionHandler.doInitialize(transactionMetadata);
            }

            Object result = null;
            try { // NOSONAR
                if (propagationResult.isNewTransactionNeeded()) {
                    LOGGER.debug("{}: beginning the JTA transaction", logPrefix);
                    userTransaction.begin();
                } else {
                    LOGGER.debug("{}: participating in an existing JTA transaction", logPrefix);
                }

                try {
                    if (userTransaction.getStatus() == Status.STATUS_ACTIVE) {
                        transactionHandler.doJoinGlobalTransaction();
                    }

                    try { // NOSONAR
                        try { // NOSONAR
                            LOGGER.debug("{}: invocation started", logPrefix);
                            result = invocation.proceed();
                            LOGGER.debug("{}: invocation ended", logPrefix);
                        } catch (Exception exception) {
                            doHandleException(logPrefix, exception, transactionMetadata, userTransaction);
                        }
                    } catch (Throwable throwable) { // NOSONAR
                        if (propagationResult.isNewTransactionNeeded()) {
                            LOGGER.debug("{}: rolling back JTA transaction after invocation exception", logPrefix);
                            userTransaction.rollback();
                        } else if (transactionMetadata.isRollbackOnParticipationFailure()) {
                            LOGGER.debug("{}: marking JTA transaction as rollback-only after invocation exception", logPrefix);
                            userTransaction.setRollbackOnly();
                        }

                        throw throwable;
                    }

                    if (propagationResult.isNewTransactionNeeded()) {
                        LOGGER.debug("{}: committing JTA transaction", logPrefix);
                        userTransaction.commit();
                    }
                } finally {
                    if (propagationResult.isNewTransactionNeeded() && userTransaction.getStatus() == Status.STATUS_ACTIVE) {
                        LOGGER.debug("{}: rolling back JTA transaction (no commit occurred)", logPrefix);
                        userTransaction.rollback();
                    }
                }
            } finally {
                if (propagationResult.isNewTransactionNeeded()) {
                    LOGGER.debug("{}: cleaning up transaction handler", logPrefix);
                    transactionHandler.doCleanup();
                }
            }

            return result;
        } finally {
            if (suspendedTransaction != null) {
                LOGGER.debug("{}: resuming suspended transaction", logPrefix);
                transactionManager.resume(suspendedTransaction);
            }
        }
    }

    protected TransactionManager getTransactionManager(String logPrefix, UserTransaction ut) {
        if (ut instanceof TransactionManager) {
            LOGGER.debug("{}: JTA UserTransaction object [{}] implements TransactionManager", logPrefix, ut);
            return (TransactionManager) ut;
        }

        if (transactionManagerName != null) {
            try {
                return (TransactionManager) jndiContext.lookup(transactionManagerName);
            } catch (NamingException e) {
                throw SeedException.wrap(e, TransactionErrorCode.UNABLE_TO_FIND_JTA_TRANSACTION_MANAGER);
            }
        }

        for (String jndiName : FALLBACK_TRANSACTION_MANAGER_NAMES) {
            try {
                TransactionManager tm = (TransactionManager) jndiContext.lookup(jndiName);
                LOGGER.debug("{}: JTA TransactionManager found at fallback JNDI location [" + jndiName + "]", logPrefix);
                return tm;
            } catch (NamingException ex) {
                LOGGER.trace("{}: no JTA TransactionManager found at fallback JNDI location [" + jndiName + "]", logPrefix);
                LOGGER.trace(CorePlugin.DETAILS_MESSAGE, ex);
            }
        }

        return null;
    }

    protected UserTransaction getUserTransaction(String logPrefix) throws NamingException {
        String jndiName = DEFAULT_USER_TRANSACTION_NAME;
        UserTransaction ut = (UserTransaction) jndiContext.lookup(jndiName);
        LOGGER.debug("{}: JTA UserTransaction found at default JNDI location [" + jndiName + "]", logPrefix);
        return ut;
    }

    private void initJTAObjects(String logPrefix) {
        if (userTransaction == null) {
            try {
                userTransaction = getUserTransaction(logPrefix);
            } catch (Exception e) {
                throw SeedException.wrap(e, TransactionErrorCode.UNABLE_TO_FIND_JTA_TRANSACTION);
            }
        }

        if (transactionManager == null) {
            transactionManager = getTransactionManager(logPrefix, userTransaction);
        }
    }

    private PropagationResult handlePropagation(Propagation propagation) throws SystemException {
        switch (propagation) {
            case MANDATORY:
                if (userTransaction.getStatus() != Status.STATUS_ACTIVE) {
                    throw SeedException.createNew(TransactionErrorCode.TRANSACTION_NEEDED_WHEN_USING_PROPAGATION_MANDATORY);
                }

                return new PropagationResult(false, false);
            case NEVER:
                if (userTransaction.getStatus() != Status.STATUS_NO_TRANSACTION) {
                    throw SeedException.createNew(TransactionErrorCode.NO_TRANSACTION_ALLOWED_WHEN_USING_PROPAGATION_NEVER);
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
                throw SeedException.createNew(TransactionErrorCode.PROPAGATION_NOT_SUPPORTED).put("propagation", propagation);
        }
    }
}
