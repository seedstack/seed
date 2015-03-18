/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */
package org.seedstack.seed.spring.internal;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

class SpringTransactionHandler implements TransactionHandler<TransactionStatus> {

    private final SpringTransactionStatusLink transactionLink;

    private final String springTransactionManagerBeanId;

    private PlatformTransactionManager transactionManager;

    @Inject
    private Injector injector;

    SpringTransactionHandler(SpringTransactionStatusLink transactionLink, String springTransactionManagerBeanId) {
        super();
        this.transactionLink = transactionLink;
        this.springTransactionManagerBeanId = springTransactionManagerBeanId;
    }

    @Override
    public void doInitialize(TransactionMetadata transactionMetadata) {
        try {
            transactionManager = injector.getInstance(Key.get(AbstractPlatformTransactionManager.class, Names.named(springTransactionManagerBeanId)));
            DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            defaultTransactionDefinition.setReadOnly(transactionMetadata.isReadOnly());
            transactionLink.push(transactionManager.getTransaction(defaultTransactionDefinition));
        } catch (TransactionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void doJoinGlobalTransaction() {
        // TODO support spring JTA
        throw new UnsupportedOperationException("Spring implementation doesn't support global transactions");
    }

    @Override
    public TransactionStatus doCreateTransaction() {
        return transactionLink.get();
    }

    @Override
    public void doBeginTransaction(TransactionStatus currentTransaction) {
        // nothing to do here
    }

    @Override
    public void doCommitTransaction(TransactionStatus currentTransaction) {
        transactionManager.commit(currentTransaction);
    }

    @Override
    public void doMarkTransactionAsRollbackOnly(TransactionStatus currentTransaction) {
        currentTransaction.setRollbackOnly();
    }

    @Override
    public void doRollbackTransaction(TransactionStatus currentTransaction) {
        transactionManager.rollback(currentTransaction);
    }

    @Override
    public void doReleaseTransaction(TransactionStatus currentTransaction) {
        // nothing to do here
    }

    @Override
    public void doCleanup() {
        transactionLink.pop();
    }

    @Override
    public TransactionStatus getCurrentTransaction() {
        return transactionLink.getCurrentTransaction();
    }

}
