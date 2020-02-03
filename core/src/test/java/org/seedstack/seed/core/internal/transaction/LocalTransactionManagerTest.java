/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.transaction.spi.TransactionManager;
import org.seedstack.seed.transaction.spi.TransactionMetadata;

public class LocalTransactionManagerTest extends AbstractTransactionManagerTest {
    private void setupCurrentTransaction() {
        when(transactionHandler.getCurrentTransaction()).thenReturn(new Object());
    }

    @Override
    protected TransactionManager doProvideTransactionManager() {
        return new LocalTransactionManager();
    }

    @Test
    public void propagation_required_without_transaction() throws Throwable {
        invoke(TransactionalMethods.Enum.REQUIRED);

        verify(transactionHandler).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler).doCreateTransaction();
        verify(transactionHandler).doBeginTransaction(any());
        verify(transactionHandler).doCommitTransaction(any());
        verify(transactionHandler, never()).doRollbackTransaction(any());
        verify(transactionHandler).doReleaseTransaction(any());
        verify(transactionHandler).doCleanup();
        verify(transactionHandler, never()).doJoinGlobalTransaction();
    }

    @Test
    public void propagation_required_with_transaction() throws Throwable {
        setupCurrentTransaction();
        invoke(TransactionalMethods.Enum.REQUIRED);

        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler, never()).doCreateTransaction();
        verify(transactionHandler, never()).doBeginTransaction(any());
        verify(transactionHandler, never()).doCommitTransaction(any());
        verify(transactionHandler, never()).doRollbackTransaction(any());
        verify(transactionHandler, never()).doReleaseTransaction(any());
        verify(transactionHandler, never()).doCleanup();
        verify(transactionHandler, never()).doJoinGlobalTransaction();
    }

    @Test
    public void propagation_mandatory_without_transaction() throws Throwable {
        try {
            invoke(TransactionalMethods.Enum.MANDATORY);
            fail("exception should have been thrown");
        } catch (SeedException e) {
            assertThat(e.getErrorCode() == TransactionErrorCode.TRANSACTION_NEEDED_WHEN_USING_PROPAGATION_MANDATORY);
        }

        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler, never()).doCreateTransaction();
        verify(transactionHandler, never()).doBeginTransaction(any());
        verify(transactionHandler, never()).doCommitTransaction(any());
        verify(transactionHandler, never()).doRollbackTransaction(any());
        verify(transactionHandler, never()).doReleaseTransaction(any());
        verify(transactionHandler, never()).doCleanup();
        verify(transactionHandler, never()).doJoinGlobalTransaction();
    }

    @Test
    public void propagation_mandatory_with_transaction() throws Throwable {
        setupCurrentTransaction();
        invoke(TransactionalMethods.Enum.MANDATORY);

        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler, never()).doCreateTransaction();
        verify(transactionHandler, never()).doBeginTransaction(any());
        verify(transactionHandler, never()).doCommitTransaction(any());
        verify(transactionHandler, never()).doRollbackTransaction(any());
        verify(transactionHandler, never()).doReleaseTransaction(any());
        verify(transactionHandler, never()).doCleanup();
        verify(transactionHandler, never()).doJoinGlobalTransaction();
    }

    @Test
    public void propagation_nested() throws Throwable {
        try {
            invoke(TransactionalMethods.Enum.NESTED);
            fail("should have thrown an exception");
        } catch (SeedException e) {
            assertThat(e.getErrorCode() == TransactionErrorCode.PROPAGATION_NOT_SUPPORTED);
        }

        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler, never()).doCreateTransaction();
        verify(transactionHandler, never()).doBeginTransaction(any());
        verify(transactionHandler, never()).doCommitTransaction(any());
        verify(transactionHandler, never()).doRollbackTransaction(any());
        verify(transactionHandler, never()).doReleaseTransaction(any());
        verify(transactionHandler, never()).doCleanup();
        verify(transactionHandler, never()).doJoinGlobalTransaction();
    }

    @Test
    public void propagation_supports_without_transaction() throws Throwable {
        invoke(TransactionalMethods.Enum.SUPPORTS);

        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler, never()).doCreateTransaction();
        verify(transactionHandler, never()).doBeginTransaction(any());
        verify(transactionHandler, never()).doCommitTransaction(any());
        verify(transactionHandler, never()).doRollbackTransaction(any());
        verify(transactionHandler, never()).doReleaseTransaction(any());
        verify(transactionHandler, never()).doCleanup();
        verify(transactionHandler, never()).doJoinGlobalTransaction();
    }

    @Test
    public void propagation_supports_with_transaction() throws Throwable {
        setupCurrentTransaction();
        invoke(TransactionalMethods.Enum.SUPPORTS);

        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler, never()).doCreateTransaction();
        verify(transactionHandler, never()).doBeginTransaction(any());
        verify(transactionHandler, never()).doCommitTransaction(any());
        verify(transactionHandler, never()).doRollbackTransaction(any());
        verify(transactionHandler, never()).doReleaseTransaction(any());
        verify(transactionHandler, never()).doCleanup();
        verify(transactionHandler, never()).doJoinGlobalTransaction();
    }

    @Test
    public void propagation_not_supported_without_transaction() throws Throwable {
        invoke(TransactionalMethods.Enum.NOT_SUPPORTED);

        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler, never()).doCreateTransaction();
        verify(transactionHandler, never()).doBeginTransaction(any());
        verify(transactionHandler, never()).doCommitTransaction(any());
        verify(transactionHandler, never()).doRollbackTransaction(any());
        verify(transactionHandler, never()).doReleaseTransaction(any());
        verify(transactionHandler, never()).doCleanup();
        verify(transactionHandler, never()).doJoinGlobalTransaction();
    }

    @Test
    public void propagation_not_supported_with_transaction() throws Throwable {
        setupCurrentTransaction();

        try {
            invoke(TransactionalMethods.Enum.NOT_SUPPORTED);
            fail("should have thrown an exception");
        } catch (SeedException e) {
            assertThat(e.getErrorCode() == TransactionErrorCode.TRANSACTION_SUSPENSION_IS_NOT_SUPPORTED);
        }

        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler, never()).doCreateTransaction();
        verify(transactionHandler, never()).doBeginTransaction(any());
        verify(transactionHandler, never()).doCommitTransaction(any());
        verify(transactionHandler, never()).doRollbackTransaction(any());
        verify(transactionHandler, never()).doReleaseTransaction(any());
        verify(transactionHandler, never()).doCleanup();
        verify(transactionHandler, never()).doJoinGlobalTransaction();
    }

    @Test
    public void propagation_never_without_transaction() throws Throwable {
        invoke(TransactionalMethods.Enum.NEVER);

        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler, never()).doCreateTransaction();
        verify(transactionHandler, never()).doBeginTransaction(any());
        verify(transactionHandler, never()).doCommitTransaction(any());
        verify(transactionHandler, never()).doRollbackTransaction(any());
        verify(transactionHandler, never()).doReleaseTransaction(any());
        verify(transactionHandler, never()).doCleanup();
        verify(transactionHandler, never()).doJoinGlobalTransaction();
    }

    @Test
    public void propagation_never_with_transaction() throws Throwable {
        setupCurrentTransaction();

        try {
            invoke(TransactionalMethods.Enum.NEVER);
            fail("should have thrown an exception");
        } catch (SeedException e) {
            assertThat(e.getErrorCode() == TransactionErrorCode.NO_TRANSACTION_ALLOWED_WHEN_USING_PROPAGATION_NEVER);
        }

        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler, never()).doCreateTransaction();
        verify(transactionHandler, never()).doBeginTransaction(any());
        verify(transactionHandler, never()).doCommitTransaction(any());
        verify(transactionHandler, never()).doRollbackTransaction(any());
        verify(transactionHandler, never()).doReleaseTransaction(any());
        verify(transactionHandler, never()).doCleanup();
        verify(transactionHandler, never()).doJoinGlobalTransaction();
    }

    @Test
    public void propagation_requires_new_without_transaction() throws Throwable {
        invoke(TransactionalMethods.Enum.REQUIRES_NEW);

        verify(transactionHandler).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler).doCreateTransaction();
        verify(transactionHandler).doBeginTransaction(any());
        verify(transactionHandler).doCommitTransaction(any());
        verify(transactionHandler, never()).doRollbackTransaction(any());
        verify(transactionHandler).doReleaseTransaction(any());
        verify(transactionHandler).doCleanup();
        verify(transactionHandler, never()).doJoinGlobalTransaction();
    }

    @Test
    public void propagation_requires_new_with_transaction() throws Throwable {
        setupCurrentTransaction();
        invoke(TransactionalMethods.Enum.REQUIRES_NEW);

        verify(transactionHandler).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler).doCreateTransaction();
        verify(transactionHandler).doBeginTransaction(any());
        verify(transactionHandler).doCommitTransaction(any());
        verify(transactionHandler, never()).doRollbackTransaction(any());
        verify(transactionHandler).doReleaseTransaction(any());
        verify(transactionHandler).doCleanup();
        verify(transactionHandler, never()).doJoinGlobalTransaction();
    }

    @Test
    public void transaction_success_should_commit() throws Throwable {
        invoke(TransactionalMethods.Enum.OK);

        verify(transactionHandler).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler).doCreateTransaction();
        verify(transactionHandler).doBeginTransaction(any());
        verify(transactionHandler).doCommitTransaction(any());
        verify(transactionHandler, never()).doRollbackTransaction(any());
        verify(transactionHandler).doReleaseTransaction(any());
        verify(transactionHandler).doCleanup();
        verify(transactionHandler, never()).doJoinGlobalTransaction();
    }

    @Test
    public void transaction_failure_should_rollback() throws Throwable {
        try {
            invoke(TransactionalMethods.Enum.FAIL);
            fail("exception should have been propagated");
        } catch (Exception e) {
            // nothing here
        }

        verify(transactionHandler).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler).doCreateTransaction();
        verify(transactionHandler).doBeginTransaction(any());
        verify(transactionHandler, never()).doCommitTransaction(any());
        verify(transactionHandler).doRollbackTransaction(any());
        verify(transactionHandler).doReleaseTransaction(any());
        verify(transactionHandler).doCleanup();
        verify(transactionHandler, never()).doJoinGlobalTransaction();
    }

    @Override
    protected void doAssertRollbackOccurred() {
        verify(transactionHandler, never()).doCommitTransaction(any());
        verify(transactionHandler).doRollbackTransaction(any());
    }

    @Override
    protected void doAssertCommitOccurred() {
        verify(transactionHandler).doCommitTransaction(any());
        verify(transactionHandler, never()).doRollbackTransaction(any());
    }
}
