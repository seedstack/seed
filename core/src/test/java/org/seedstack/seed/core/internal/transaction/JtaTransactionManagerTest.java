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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.naming.Context;
import javax.transaction.Status;
import mockit.Deencapsulation;
import org.junit.Test;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.transaction.TransactionConfig;
import org.seedstack.seed.transaction.spi.TransactionManager;
import org.seedstack.seed.transaction.spi.TransactionMetadata;

@SuppressWarnings("unchecked")
public class JtaTransactionManagerTest extends AbstractTransactionManagerTest {
    private UserTransactionMock userTransaction;
    private TransactionManagerMock transactionManager;

    private void setupActiveTransaction() {
        userTransaction.begin();
    }

    private void verifyNoDirectTransactionHandling() {
        verify(transactionHandler, never()).doCreateTransaction();
        verify(transactionHandler, never()).doBeginTransaction(any());
        verify(transactionHandler, never()).doCommitTransaction(any());
        verify(transactionHandler, never()).doRollbackTransaction(any());
        verify(transactionHandler, never()).doReleaseTransaction(any());
    }

    @Override
    protected TransactionManager doProvideTransactionManager() throws Exception {
        userTransaction = new UserTransactionMock();
        transactionManager = new TransactionManagerMock(userTransaction);

        Context jndiContext = mock(Context.class);
        when(jndiContext.lookup("java:comp/UserTransaction")).thenReturn(userTransaction);
        when(jndiContext.lookup("java:comp/TransactionManager")).thenReturn(transactionManager);

        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        Deencapsulation.setField(jtaTransactionManager,
                "jndiContext",
                jndiContext);
        Deencapsulation.setField(jtaTransactionManager,
                "jtaConfig",
                new TransactionConfig.JtaConfig());

        return jtaTransactionManager;
    }

    @Test
    public void propagation_required_without_transaction() throws Throwable {
        invoke(TransactionalMethods.Enum.REQUIRED);

        verifyNoDirectTransactionHandling();
        verify(transactionHandler).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler).doJoinGlobalTransaction();
        verify(transactionHandler).doCleanup();
        assertThat(transactionManager.getSuspendCallCount()).isEqualTo(0);
        assertThat(transactionManager.getResumeCallCount()).isEqualTo(0);
    }

    @Test
    public void propagation_required_with_transaction() throws Throwable {
        setupActiveTransaction();
        invoke(TransactionalMethods.Enum.REQUIRED);

        verifyNoDirectTransactionHandling();
        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler).doJoinGlobalTransaction();
        verify(transactionHandler, never()).doCleanup();
        assertThat(transactionManager.getSuspendCallCount()).isEqualTo(0);
        assertThat(transactionManager.getResumeCallCount()).isEqualTo(0);
    }

    @Test
    public void propagation_mandatory_without_transaction() throws Throwable {
        try {
            invoke(TransactionalMethods.Enum.MANDATORY);
            fail("exception should have been thrown");
        } catch (SeedException e) {
            assertThat(e.getErrorCode() == TransactionErrorCode.TRANSACTION_NEEDED_WHEN_USING_PROPAGATION_MANDATORY);
        }

        verifyNoDirectTransactionHandling();
        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler, never()).doJoinGlobalTransaction();
        verify(transactionHandler, never()).doCleanup();
        assertThat(transactionManager.getSuspendCallCount()).isEqualTo(0);
        assertThat(transactionManager.getResumeCallCount()).isEqualTo(0);
    }

    @Test
    public void propagation_mandatory_with_transaction() throws Throwable {
        setupActiveTransaction();
        invoke(TransactionalMethods.Enum.MANDATORY);

        verifyNoDirectTransactionHandling();
        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler).doJoinGlobalTransaction();
        verify(transactionHandler, never()).doCleanup();
        assertThat(transactionManager.getSuspendCallCount()).isEqualTo(0);
        assertThat(transactionManager.getResumeCallCount()).isEqualTo(0);
    }

    @Test
    public void propagation_nested() throws Throwable {
        try {
            invoke(TransactionalMethods.Enum.NESTED);
            fail("should have thrown an exception");
        } catch (SeedException e) {
            assertThat(e.getErrorCode() == TransactionErrorCode.PROPAGATION_NOT_SUPPORTED);
        }

        verifyNoDirectTransactionHandling();
        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler, never()).doJoinGlobalTransaction();
        verify(transactionHandler, never()).doCleanup();
        assertThat(transactionManager.getSuspendCallCount()).isEqualTo(0);
        assertThat(transactionManager.getResumeCallCount()).isEqualTo(0);
    }

    @Test
    public void propagation_supports_without_transaction() throws Throwable {
        invoke(TransactionalMethods.Enum.SUPPORTS);

        verifyNoDirectTransactionHandling();
        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler, never()).doJoinGlobalTransaction();
        verify(transactionHandler, never()).doCleanup();
        assertThat(transactionManager.getSuspendCallCount()).isEqualTo(0);
        assertThat(transactionManager.getResumeCallCount()).isEqualTo(0);
    }

    @Test
    public void propagation_supports_with_transaction() throws Throwable {
        setupActiveTransaction();
        invoke(TransactionalMethods.Enum.SUPPORTS);

        verifyNoDirectTransactionHandling();
        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler).doJoinGlobalTransaction();
        verify(transactionHandler, never()).doCleanup();
        assertThat(transactionManager.getSuspendCallCount()).isEqualTo(0);
        assertThat(transactionManager.getResumeCallCount()).isEqualTo(0);
    }

    @Test
    public void propagation_not_supported_without_transaction() throws Throwable {
        invoke(TransactionalMethods.Enum.NOT_SUPPORTED);

        verifyNoDirectTransactionHandling();
        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler, never()).doJoinGlobalTransaction();
        verify(transactionHandler, never()).doCleanup();
        assertThat(transactionManager.getSuspendCallCount()).isEqualTo(0);
        assertThat(transactionManager.getResumeCallCount()).isEqualTo(0);
    }

    @Test
    public void propagation_not_supported_with_transaction() throws Throwable {
        setupActiveTransaction();
        invoke(TransactionalMethods.Enum.NOT_SUPPORTED);

        verifyNoDirectTransactionHandling();
        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler, never()).doJoinGlobalTransaction();
        verify(transactionHandler, never()).doCleanup();
        assertThat(transactionManager.getSuspendCallCount()).isEqualTo(1);
        assertThat(transactionManager.getResumeCallCount()).isEqualTo(1);
    }

    @Test
    public void propagation_never_without_transaction() throws Throwable {
        invoke(TransactionalMethods.Enum.NEVER);

        verifyNoDirectTransactionHandling();
        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler, never()).doJoinGlobalTransaction();
        verify(transactionHandler, never()).doCleanup();
        assertThat(transactionManager.getSuspendCallCount()).isEqualTo(0);
        assertThat(transactionManager.getResumeCallCount()).isEqualTo(0);
    }

    @Test
    public void propagation_never_with_transaction() throws Throwable {
        setupActiveTransaction();

        try {
            invoke(TransactionalMethods.Enum.NEVER);
            fail("should have thrown an exception");
        } catch (SeedException e) {
            assertThat(e.getErrorCode() == TransactionErrorCode.NO_TRANSACTION_ALLOWED_WHEN_USING_PROPAGATION_NEVER);
        }

        verifyNoDirectTransactionHandling();
        verify(transactionHandler, never()).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler, never()).doJoinGlobalTransaction();
        verify(transactionHandler, never()).doCleanup();
        assertThat(transactionManager.getSuspendCallCount()).isEqualTo(0);
        assertThat(transactionManager.getResumeCallCount()).isEqualTo(0);
    }

    @Test
    public void propagation_requires_new_without_transaction() throws Throwable {
        invoke(TransactionalMethods.Enum.REQUIRES_NEW);

        verifyNoDirectTransactionHandling();
        verify(transactionHandler).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler).doJoinGlobalTransaction();
        verify(transactionHandler).doCleanup();
        assertThat(transactionManager.getSuspendCallCount()).isEqualTo(0);
        assertThat(transactionManager.getResumeCallCount()).isEqualTo(0);
    }

    @Test
    public void propagation_requires_new_with_transaction() throws Throwable {
        setupActiveTransaction();
        invoke(TransactionalMethods.Enum.REQUIRES_NEW);

        verifyNoDirectTransactionHandling();
        verify(transactionHandler).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler).doJoinGlobalTransaction();
        verify(transactionHandler).doCleanup();
        assertThat(transactionManager.getSuspendCallCount()).isEqualTo(1);
        assertThat(transactionManager.getResumeCallCount()).isEqualTo(1);
    }

    @Test
    public void transaction_success_should_commit() throws Throwable {
        invoke(TransactionalMethods.Enum.OK);

        verifyNoDirectTransactionHandling();
        verify(transactionHandler).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler).doJoinGlobalTransaction();
        verify(transactionHandler).doCleanup();
        assertThat(userTransaction.getStatus()).isEqualTo(Status.STATUS_COMMITTED);
        assertThat(transactionManager.getSuspendCallCount()).isEqualTo(0);
        assertThat(transactionManager.getResumeCallCount()).isEqualTo(0);
    }

    @Test
    public void transaction_failure_should_rollback() throws Throwable {
        try {
            invoke(TransactionalMethods.Enum.FAIL);
            fail("exception should have been propagated");
        } catch (Exception e) {
            // nothing here
        }

        verifyNoDirectTransactionHandling();
        verify(transactionHandler).doInitialize(any(TransactionMetadata.class));
        verify(transactionHandler).doJoinGlobalTransaction();
        verify(transactionHandler).doCleanup();
        assertThat(userTransaction.getStatus()).isEqualTo(Status.STATUS_ROLLEDBACK);
        assertThat(transactionManager.getSuspendCallCount()).isEqualTo(0);
        assertThat(transactionManager.getResumeCallCount()).isEqualTo(0);
    }

    @Override
    protected void doAssertRollbackOccurred() {
        assertThat(userTransaction.getStatus()).isEqualTo(Status.STATUS_ROLLEDBACK);
    }

    @Override
    protected void doAssertCommitOccurred() {
        assertThat(userTransaction.getStatus()).isEqualTo(Status.STATUS_COMMITTED);
    }
}
