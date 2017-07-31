/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.transaction;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import org.seedstack.seed.core.internal.utils.MethodMatcherBuilder;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionManager;
import org.seedstack.seed.transaction.spi.TransactionMetadata;
import org.seedstack.seed.transaction.spi.TransactionMetadataResolver;

import java.lang.reflect.Method;
import java.util.Set;

@TransactionConcern
class TransactionModule extends AbstractModule {
    private final TransactionManager transactionManager;
    private final Class<? extends TransactionHandler> defaultTransactionHandlerClass;
    private final Set<Class<? extends TransactionMetadataResolver>> transactionMetadataResolvers;

    TransactionModule(TransactionManager transactionManager, Class<? extends TransactionHandler> defaultTransactionHandlerClass, Set<Class<? extends TransactionMetadataResolver>> transactionMetadataResolvers) {
        this.transactionManager = transactionManager;
        this.defaultTransactionHandlerClass = defaultTransactionHandlerClass;
        this.transactionMetadataResolvers = transactionMetadataResolvers;
    }

    @Override
    protected void configure() {
        Multibinder<TransactionMetadataResolver> transactionMetadataResolverMultibinder = Multibinder.newSetBinder(binder(), TransactionMetadataResolver.class);
        for (Class<? extends TransactionMetadataResolver> transactionMetadataResolver : transactionMetadataResolvers) {
            transactionMetadataResolverMultibinder.addBinding().to(transactionMetadataResolver);
        }

        if (defaultTransactionHandlerClass != null) {
            bind(new DefaultTransactionHandlerTypeLiteral()).annotatedWith(Names.named("default")).toInstance(defaultTransactionHandlerClass);
        } else {
            bind(new DefaultTransactionHandlerTypeLiteral()).annotatedWith(Names.named("default")).toProvider(Providers.of(null));
        }

        requestInjection(transactionManager);
        bindInterceptor(Matchers.any(), buildMethodMatcher(), transactionManager.getMethodInterceptor());
        bind(TransactionManager.class).toInstance(transactionManager);
        bind(TransactionMetadata.class);
    }

    private static Matcher<Method> buildMethodMatcher() {
        MethodMatcherBuilder methodMatcherBuilder = new MethodMatcherBuilder(TransactionalResolver.INSTANCE);
        if (TransactionPlugin.JTA_12_OPTIONAL.isPresent()) {
            methodMatcherBuilder.or(JtaTransactionalResolver.INSTANCE);
        }
        return methodMatcherBuilder.build();
    }

    private static class DefaultTransactionHandlerTypeLiteral extends TypeLiteral<Class<? extends TransactionHandler>> {
    }
}
