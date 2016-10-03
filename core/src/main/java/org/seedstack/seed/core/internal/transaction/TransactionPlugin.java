/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.transaction;

import com.google.inject.matcher.Matcher;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.core.utils.SeedMatchers;
import org.seedstack.seed.transaction.Transactional;
import org.seedstack.seed.transaction.TransactionConfig;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionManager;
import org.seedstack.seed.transaction.spi.TransactionMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This plugin manages transactional matters in SEED code:
 * <ul>
 * <li>detects transaction-enabled methods and add the corresponding interceptor around them,</li>
 * <li>holds a registry of all transaction handlers,</li>
 * <li>provides various utilities for implementing transactional behavior.</li>
 * </ul>
 *
 * @author adrien.lauer@mpsa.com
 */
public class TransactionPlugin extends AbstractSeedPlugin {
    static final Matcher<Method> TRANSACTIONAL_MATCHER = SeedMatchers.methodOrAncestorMetaAnnotatedWith(Transactional.class).and(SeedMatchers.methodNotSynthetic()).and(SeedMatchers.methodNotOfObject());
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionPlugin.class);
    private final Set<Class<? extends TransactionMetadataResolver>> transactionMetadataResolvers = new HashSet<>();
    private TransactionManager transactionManager;
    private Class<? extends TransactionHandler> defaultTransactionHandlerClass;

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

        Class<? extends TransactionManager> txManager = transactionConfig.getManager();
        if (txManager == null) {
            txManager = LocalTransactionManager.class;
        }
        try {
            this.transactionManager = txManager.newInstance();
        } catch (Exception e) {
            throw new PluginException("Unable to instantiate transaction manager from class " + txManager, e);
        }
        this.defaultTransactionHandlerClass = transactionConfig.getDefaultHandler();

        initContext.scannedSubTypesByParentClass().get(TransactionMetadataResolver.class).stream()
                .filter(TransactionMetadataResolver.class::isAssignableFrom)
                .forEach(candidate -> {
                    transactionMetadataResolvers.add(candidate.asSubclass(TransactionMetadataResolver.class));
                    LOGGER.trace("Detected transaction metadata resolver {}", candidate.getCanonicalName());
                });
        LOGGER.debug("Detected {} transaction metadata resolver(s)", transactionMetadataResolvers.size());

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new TransactionModule(
                this.transactionManager,
                this.defaultTransactionHandlerClass,
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
        return TRANSACTIONAL_MATCHER.matches(method);
    }
}
