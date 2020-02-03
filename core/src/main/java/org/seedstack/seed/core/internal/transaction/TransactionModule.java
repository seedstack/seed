/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.transaction;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import java.lang.reflect.Method;
import java.util.Set;
import org.seedstack.seed.core.internal.utils.MethodMatcherBuilder;
import org.seedstack.seed.transaction.spi.TransactionManager;
import org.seedstack.seed.transaction.spi.TransactionMetadataResolver;

@TransactionConcern
class TransactionModule extends AbstractModule {
    private final TransactionManager transactionManager;
    private final Set<Class<? extends TransactionMetadataResolver>> transactionMetadataResolvers;

    TransactionModule(TransactionManager transactionManager,
            Set<Class<? extends TransactionMetadataResolver>> transactionMetadataResolvers) {
        this.transactionManager = transactionManager;
        this.transactionMetadataResolvers = transactionMetadataResolvers;
    }

    private static Matcher<Method> buildMethodMatcher() {
        MethodMatcherBuilder methodMatcherBuilder = new MethodMatcherBuilder(TransactionalResolver.INSTANCE);
        if (TransactionPlugin.JTA_12_OPTIONAL.isPresent()) {
            methodMatcherBuilder.or(JtaTransactionalResolver.INSTANCE);
        }
        return methodMatcherBuilder.build();
    }

    @Override
    protected void configure() {
        Multibinder<TransactionMetadataResolver> transactionMetadataResolverMultibinder = Multibinder
                .newSetBinder(binder(), TransactionMetadataResolver.class);

        for (Class<? extends TransactionMetadataResolver> transactionMetadataResolver : transactionMetadataResolvers) {
            transactionMetadataResolverMultibinder.addBinding().to(transactionMetadataResolver);
        }

        requestInjection(transactionManager);
        bindInterceptor(Matchers.any(), buildMethodMatcher(), transactionManager.getMethodInterceptor());
        bind(TransactionManager.class).toInstance(transactionManager);
    }
}
