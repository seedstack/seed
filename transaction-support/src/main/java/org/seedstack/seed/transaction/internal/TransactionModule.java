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

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import org.seedstack.seed.transaction.spi.TransactionConcern;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionManager;
import org.seedstack.seed.transaction.spi.TransactionMetadata;
import org.seedstack.seed.transaction.spi.TransactionMetadataResolver;

import java.util.Set;

@TransactionConcern
class TransactionModule extends AbstractModule {
    private final TransactionManager transactionManager;
    private final Class<? extends TransactionHandler> defaultTransactionHandlerClass;
    private final Set<Class<? extends TransactionMetadataResolver>> transactionMetadataResolvers;
    private final Set<Class<? extends TransactionHandler>> registeredTransactionHandlers;

    TransactionModule(TransactionManager transactionManager, Class<? extends TransactionHandler> defaultTransactionHandlerClass, Set<Class<? extends TransactionMetadataResolver>> transactionMetadataResolvers, Set<Class<? extends TransactionHandler>> registeredTransactionHandlers) {
        this.transactionManager = transactionManager;
        this.defaultTransactionHandlerClass = defaultTransactionHandlerClass;
        this.transactionMetadataResolvers = transactionMetadataResolvers;
        this.registeredTransactionHandlers = registeredTransactionHandlers;
    }

    @Override
    protected void configure() {
        bind(TransactionMetadata.class);

        Multibinder<TransactionMetadataResolver> transactionMetadataResolverMultibinder = Multibinder.newSetBinder(binder(), TransactionMetadataResolver.class);
        for (Class<? extends TransactionMetadataResolver> transactionMetadataResolver : transactionMetadataResolvers) {
            transactionMetadataResolverMultibinder.addBinding().to(transactionMetadataResolver);
        }

        Class<? extends TransactionHandler> resultingDefaultTransactionHandlerClass = null;
        if (defaultTransactionHandlerClass != null) {
            resultingDefaultTransactionHandlerClass = defaultTransactionHandlerClass;
        } else if (this.registeredTransactionHandlers.size() == 1) {
            resultingDefaultTransactionHandlerClass = this.registeredTransactionHandlers.iterator().next();
        }

        if (resultingDefaultTransactionHandlerClass != null) {
            bind(new TypeLiteral<Class<? extends TransactionHandler>>() {
            }).annotatedWith(Names.named("default")).toInstance(resultingDefaultTransactionHandlerClass);
        } else {
            bind(new TypeLiteral<Class<? extends TransactionHandler>>() {
            }).annotatedWith(Names.named("default")).toProvider(Providers.<Class<? extends TransactionHandler>>of(null));
        }

        requestInjection(transactionManager);

        bindInterceptor(Matchers.any(), TransactionPlugin.TRANSACTIONAL_MATCHER, transactionManager.getMethodInterceptor());

        bind(TransactionManager.class).toInstance(transactionManager);
    }
}
