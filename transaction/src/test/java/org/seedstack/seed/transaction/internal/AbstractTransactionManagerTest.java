/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.transaction.internal;

import com.google.common.collect.Sets;
import com.google.inject.Injector;
import org.seedstack.seed.transaction.spi.ExceptionHandler;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionManager;
import org.seedstack.seed.transaction.spi.TransactionMetadata;
import org.seedstack.seed.transaction.spi.TransactionMetadataResolver;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public abstract class AbstractTransactionManagerTest {
    protected TransactionMetadata transactionMetadata;
    protected TransactionMetadataResolver transactionMetadataResolver;
    protected TransactionHandler transactionHandler;

    private ExceptionHandler exceptionHandler;
    private TransactionManager underTest;

    @Before
    public void before() throws Exception {
        transactionHandler = mock(TransactionHandler.class);

        exceptionHandler = mock(ExceptionHandler.class);

        transactionMetadata = new TransactionMetadata().defaults();
        transactionMetadata.setHandler(TransactionHandler.class);

        transactionMetadataResolver = mock(TransactionMetadataResolver.class);
        when(transactionMetadataResolver.resolve(any(MethodInvocation.class), any(TransactionMetadata.class))).thenReturn(transactionMetadata);

        Injector injector = mock(Injector.class);
        when(injector.getInstance(TransactionHandler.class)).thenReturn(transactionHandler);
        when(injector.getInstance(TransactionMetadata.class)).thenReturn(new TransactionMetadata());
        when(injector.getInstance(ExceptionHandler.class)).thenReturn(exceptionHandler);

        underTest = doProvideTransactionManager();
        Whitebox.setInternalState(underTest, "transactionMetadataResolvers", Sets.newHashSet(transactionMetadataResolver));
        Whitebox.setInternalState(underTest, "injector", injector);
    }

    protected abstract TransactionManager doProvideTransactionManager() throws Exception;

    protected abstract void doAssertRollbackOccurred() throws Exception;

    protected abstract void doAssertCommitOccurred() throws Exception;

    protected void invoke(TransactionalMethods.Enum methodToCall) throws Throwable {
        underTest.getMethodInterceptor().invoke(methodToCall.getMethodInvocation());
    }

    protected void invokeWithArguments(TransactionalMethods.Enum methodToCall, Object[] arguments) throws Throwable {
        MethodInvocation methodInvocation = methodToCall.getMethodInvocation();
        ((SimpleMethodInvocation) methodInvocation).setArguments(arguments);
        underTest.getMethodInterceptor().invoke(methodInvocation);
    }

    @Test
    public void exception_handler_can_mask_exception() throws Throwable {
        transactionMetadata.setExceptionHandler(ExceptionHandler.class);
        when(exceptionHandler.handleException(any(Exception.class), any(TransactionMetadata.class), any())).thenReturn(true);

        try {
            invoke(TransactionalMethods.Enum.FAIL);
        } catch (Exception e) {
            fail("exception should have been handled");
        }

        verify(exceptionHandler).handleException(any(Exception.class), any(TransactionMetadata.class), any());
    }

    @Test
    public void exception_handler_can_process_exception_without_masking_it() throws Throwable {
        transactionMetadata.setExceptionHandler(ExceptionHandler.class);
        when(exceptionHandler.handleException(any(Exception.class), any(TransactionMetadata.class), any())).thenReturn(false);

        try {
            invoke(TransactionalMethods.Enum.FAIL);
            fail("exception should have been propagated");
        } catch (Exception e) {
            // nothing here
        }

        verify(exceptionHandler).handleException(any(Exception.class), any(TransactionMetadata.class), any());
    }

    @Test
    public void rollback_on_all_exceptions_by_default() throws Throwable {
        try {
            invokeWithArguments(TransactionalMethods.Enum.DEFAULT_ROLLBACK, new Object[]{new MyException()});
            fail("exception should have been propagated");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(MyException.class);
        }

        doAssertRollbackOccurred();
    }

    @Test
    public void no_rollback_on_exceptions_not_matching_rollback_for() throws Throwable {
        try {
            invokeWithArguments(TransactionalMethods.Enum.ROLLBACK_FOR, new Object[]{new MyException()});
        } catch (Exception e) {
            fail("exception should not have been propagated");
        }

        doAssertCommitOccurred();
    }

    @Test
    public void rollback_on_exceptions_matching_rollback_for() throws Throwable {
        try {
            invokeWithArguments(TransactionalMethods.Enum.ROLLBACK_FOR, new Object[]{new IllegalArgumentException()});
            fail("exception should have been propagated");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }

        doAssertRollbackOccurred();
    }

    @Test
    public void rollback_on_exceptions_matching_rollback_for_but_not_matching_no_rollback_for() throws Throwable {
        try {
            invokeWithArguments(TransactionalMethods.Enum.NO_ROLLBACK_FOR, new Object[]{new MyException()});
            fail("exception should have been propagated");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(MyException.class);
        }

        doAssertRollbackOccurred();
    }

    @Test
    public void no_rollback_on_exceptions_matching_rollback_for_and_matching_no_rollback_for_too() throws Throwable {
        try {
            invokeWithArguments(TransactionalMethods.Enum.NO_ROLLBACK_FOR, new Object[]{new IllegalArgumentException()});
        } catch (Exception e) {
            fail("exception should not have been propagated");
        }

        doAssertCommitOccurred();
    }

    @Test
    public void error_always_results_in_rollback() throws Throwable {
        try {
            invokeWithArguments(TransactionalMethods.Enum.DEFAULT_ROLLBACK, new Object[]{new MyError()});
            fail("error should have been propagated");
        } catch (Throwable t) {
            assertThat(t).isInstanceOf(MyError.class);
        }

        doAssertRollbackOccurred();
    }

    private class MyException extends Exception {}
    private class MyError extends Error {}
}
