/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.transaction.internal;

import com.google.inject.matcher.Matcher;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.core.utils.SeedMatchers;
import org.seedstack.seed.transaction.api.Transactional;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionManager;
import org.seedstack.seed.transaction.spi.TransactionMetadataResolver;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This plugin manages transactional matters in SEED code:
 *
 * <ul>
 * <li>detects transaction-enabled methods and add the corresponding interceptor around them,</li>
 * <li>holds a registry of all transaction handlers,</li>
 * <li>provides various utilities for implementing transactional behavior.</li>
 * </ul>
 *
 * @author adrien.lauer@mpsa.com
 */
public class TransactionPlugin extends AbstractPlugin {
    public static final String TRANSACTION_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.seed.transaction";
    public static final Matcher<Method> TRANSACTIONAL_MATCHER = SeedMatchers.methodOrAncestorMetaAnnotatedWith(Transactional.class).and(SeedMatchers.methodNotSynthetic()).and(SeedMatchers.methodNotOfObject());
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionPlugin.class);

    private final Set<Class<? extends TransactionHandler>> registeredTransactionHandlers = new HashSet<Class<? extends TransactionHandler>>();
    private final Set<Class<? extends TransactionMetadataResolver>> transactionMetadataResolvers = new HashSet<Class<? extends TransactionMetadataResolver>>();

    private TransactionManager transactionManager;
    private Class<? extends TransactionHandler> defaultTransactionHandlerClass;

    @Override
    public String name() {
        return "seed-transaction-plugin";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InitState init(InitContext initContext) {
        ApplicationPlugin applicationPlugin = (ApplicationPlugin) initContext.pluginsRequired().iterator().next();
        Configuration transactionConfiguration = applicationPlugin.getApplication().getConfiguration().subset(TransactionPlugin.TRANSACTION_PLUGIN_CONFIGURATION_PREFIX);

        String transactionManagerClassname = transactionConfiguration.getString("manager");
        if (transactionManagerClassname != null && !transactionManagerClassname.isEmpty()) {
            try {
                this.transactionManager = (TransactionManager) Class.forName(transactionManagerClassname).newInstance();
            } catch (Exception e) {
                throw new PluginException("Unable to instantiate transaction manager from class " + transactionManagerClassname, e);
            }
        } else {
            this.transactionManager = new LocalTransactionManager();
        }

        String defaultTransactionHandlerClassname = transactionConfiguration.getString("default-handler");
        if (StringUtils.isNotBlank(defaultTransactionHandlerClassname)) {
            try {
                this.defaultTransactionHandlerClass = (Class<? extends TransactionHandler>) Class.forName(defaultTransactionHandlerClassname);
            } catch (ClassNotFoundException e) {
                throw new PluginException("Unable to load default transaction handler class " + defaultTransactionHandlerClassname, e);
            }
        }

        Collection<Class<?>> scannedTransactionMetadataResolverClasses = initContext.scannedSubTypesByParentClass().get(TransactionMetadataResolver.class);
        if (scannedTransactionMetadataResolverClasses != null) {
            for (Class<?> candidate : scannedTransactionMetadataResolverClasses) {
                if (TransactionMetadataResolver.class.isAssignableFrom(candidate)) {
                    transactionMetadataResolvers.add((Class<? extends TransactionMetadataResolver>) candidate);
                    LOGGER.trace("Detected transaction metadata resolver {}", candidate.getCanonicalName());
                }
            }
        }

        LOGGER.debug("Detected {} transaction metadata resolver", transactionMetadataResolvers.size());

        return InitState.INITIALIZED;
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().subtypeOf(TransactionMetadataResolver.class).build();
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        return plugins;
    }

    @Override
    public Object nativeUnitModule() {
        return new TransactionModule(
                this.transactionManager,
                this.defaultTransactionHandlerClass,
                this.transactionMetadataResolvers,
                this.registeredTransactionHandlers
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

    /**
     * Register a transaction handler. Must be called by supports plugin implementing transactional behavior.
     *
     * @param transactionHandler the transaction handler to register.
     */
    public void registerTransactionHandler(Class<? extends TransactionHandler> transactionHandler) {
        LOGGER.trace("Registered transaction handler {}", transactionHandler.getCanonicalName());
        this.registeredTransactionHandlers.add(transactionHandler);
    }
}
