/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.transaction.spi;

/**
 * Classes implementing this interface can be used by a SeedStack transaction manager to handle a specific kind of
 * transaction (e.g. JPA, JMS, ...). Any of the  do*() methods can be implemented with an empty body if they
 * are not applicable in this kind of transaction context.
 *
 * @param <T> The transaction object if any, object otherwise
 */
public interface TransactionHandler<T> {
    /**
     * This method is called before the transaction creation and is responsible to execution any initialization
     * code necessary to the underlying implementation. If this method fails, no cleanup is performed so implementations
     * must ensure that its behavior is atomic (all or nothing).
     * <p>The transactionMetadata is given by the {@link TransactionManager}.</p>
     *
     * @param transactionMetadata The associated transaction metadata.
     */
    void doInitialize(TransactionMetadata transactionMetadata);

    /**
     * This method is called on transaction creation and is responsible to return the object instance representing
     * the new transaction.
     *
     * @return the new transaction object, null if none.
     */
    T doCreateTransaction();

    /**
     * This method is called when joining a global transaction is required by the transaction manager.
     */
    void doJoinGlobalTransaction();

    /**
     * This method is called on transaction startup and marks the beginning of the transaction.
     *
     * @param currentTransaction The transaction object as returned by doCreateTransaction(), null if none.
     */
    void doBeginTransaction(T currentTransaction);

    /**
     * This method is called when the transaction needs to be committed according to the policy specified by the
     * transaction metadata.
     *
     * @param currentTransaction The transaction object as returned by doCreateTransaction(), null if none.
     */
    void doCommitTransaction(T currentTransaction);

    /**
     * This method is called when the transaction needs to be marked as rollback only according to the policy specified
     * by the transaction metadata.
     *
     * @param currentTransaction The transaction object as returned by doCreateTransaction(), null if none.
     */
    void doMarkTransactionAsRollbackOnly(T currentTransaction);

    /**
     * This method is called when the transaction needs to be rollbacked according to the policy specified by the
     * transaction metadata.
     *
     * @param currentTransaction The transaction object as returned by doCreateTransaction(), null if none.
     */
    void doRollbackTransaction(T currentTransaction);

    /**
     * This method is called on transaction end to release the transaction object if needed. Note that it is called
     * even if the transaction failed to begin.
     *
     * @param currentTransaction The transaction object as returned by doCreateTransaction(), null if none.
     */
    void doReleaseTransaction(T currentTransaction);

    /**
     * This method is invoked after transaction has ended (either successfully or not) and is responsible to clean
     * anything initialized by the doInitialize() method.
     */
    void doCleanup();

    /**
     * This method is invoked before transaction initialization to check if a transaction already exists and can
     * eventually be reused.
     *
     * @return the transaction object.
     */
    T getCurrentTransaction();
}
