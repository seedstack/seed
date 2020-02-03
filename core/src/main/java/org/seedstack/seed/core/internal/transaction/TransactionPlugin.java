/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.transaction;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.transaction.Transactional;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.transaction.TransactionConfig;
import org.seedstack.seed.transaction.spi.TransactionManager;
import org.seedstack.seed.transaction.spi.TransactionMetadataResolver;
import org.seedstack.shed.reflect.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This plugin manages transactional matters in SeedStack code:
 * <ul>
 * <li>detects transaction-enabled methods and add the corresponding interceptor around them,</li>
 * <li>holds a registry of all transaction handlers,</li>
 * <li>provides various utilities for implementing transactional behavior.</li>
 * </ul>
 */
public class TransactionPlugin extends AbstractSeedPlugin {
    static final Optional<Class<Transactional>> JTA_12_OPTIONAL = Classes.optional("javax.transaction.Transactional");
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionPlugin.class);
    private final Set<Class<? extends TransactionMetadataResolver>> transactionMetadataResolvers = new HashSet<>();
    private TransactionManager transactionManager;

    @Override
    public String name() {
        return "transaction";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().subtypeOf(TransactionMetadataResolver.class).build();
    }

    @Override
    public InitState initialize(InitContext initContext) {
        TransactionConfig transactionConfig = getConfiguration(TransactionConfig.class);
        Class<? extends TransactionManager> txManagerClass = transactionConfig.getManager();
        if (txManagerClass == null) {
            txManagerClass = LocalTransactionManager.class;
        }
        this.transactionManager = Classes.instantiateDefault(txManagerClass);

        initContext.scannedSubTypesByParentClass().get(TransactionMetadataResolver.class).stream()
                .filter(TransactionMetadataResolver.class::isAssignableFrom)
                .forEach(candidate -> {
                    transactionMetadataResolvers.add(candidate.asSubclass(TransactionMetadataResolver.class));
                    LOGGER.debug("Transaction metadata resolver {} detected", candidate.getCanonicalName());
                });

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new TransactionModule(
                this.transactionManager,
                this.transactionMetadataResolvers
        );
    }

    /**
     * Checks if a method is detected as transactional by this plugin.
     *
     * @param method the method to check.
     * @return true if the method is candidate to be transactional, false otherwise.
     */
    public boolean isTransactional(Method method) {
        return TransactionalResolver.INSTANCE.test(method);
    }
}
