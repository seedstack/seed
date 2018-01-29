/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.transaction;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.transaction.Propagation;
import org.seedstack.seed.transaction.TransactionConfig;
import org.seedstack.seed.transaction.Transactional;
import org.seedstack.seed.transaction.spi.ExceptionHandler;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionManager;
import org.seedstack.seed.transaction.spi.TransactionMetadata;
import org.seedstack.seed.transaction.spi.TransactionMetadataResolver;

/**
 * Base class for common transaction manager behavior.
 */
public abstract class AbstractTransactionManager implements TransactionManager {
    private final MethodInterceptorImplementation methodInterceptorImplementation = new
            MethodInterceptorImplementation();
    @Inject
    protected Injector injector;
    @Inject
    private Set<TransactionMetadataResolver> transactionMetadataResolvers;
    @Configuration
    private TransactionConfig transactionConfig;

    @Override
    public MethodInterceptor getMethodInterceptor() {
        return methodInterceptorImplementation;
    }

    /**
     * This method provide the technology-specific interception behavior.
     *
     * @param transactionLogger   The object that must be used to log transaction progress.
     * @param invocation          The method interception object.
     * @param transactionMetadata Metadata of the current transaction.
     * @param transactionHandler  Transaction handler for the current transacted resource.
     * @return the value of the method invocation
     * @throws Throwable if any problem occurs during interception.
     */
    protected abstract <T> Object doMethodInterception(TransactionLogger transactionLogger, MethodInvocation invocation,
            TransactionMetadata transactionMetadata, TransactionHandler<T> transactionHandler) throws Throwable;

    /**
     * This method call the wrapped transactional method.
     *
     * @param transactionLogger   The object that must be used to log transaction progress.
     * @param invocation          the {@link MethodInvocation} denoting the transactional method.
     * @param transactionMetadata the current transaction metadata.
     * @param currentTransaction  the current transaction object if any.
     * @return the return value of the transactional method.
     * @throws Throwable if an exception occurs during the method invocation.
     */
    protected Object doInvocation(TransactionLogger transactionLogger, MethodInvocation invocation,
            TransactionMetadata transactionMetadata, Object currentTransaction) throws Throwable {
        Object result = null;
        try {
            transactionLogger.log("invocation started", transactionLogger);
            result = invocation.proceed();
            transactionLogger.log("invocation ended", transactionLogger);
        } catch (Exception exception) {
            doHandleException(transactionLogger, exception, transactionMetadata, currentTransaction);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void doHandleException(TransactionLogger transactionLogger, Exception exception,
            TransactionMetadata transactionMetadata, Object currentTransaction) throws Exception {
        boolean matchForRollback = false, matchForNoRollback = false;

        // Check for the need to rollback
        for (Class<? extends Exception> rollbackExceptionClass : transactionMetadata.getRollbackOn()) {
            if (rollbackExceptionClass.isAssignableFrom(exception.getClass())) {
                matchForRollback = true;

                for (Class<? extends Exception> noRollbackExceptionClass : transactionMetadata.getNoRollbackFor()) {
                    if (noRollbackExceptionClass.isAssignableFrom(exception.getClass())) {
                        matchForNoRollback = true;
                    }
                }
            }
        }

        if (matchForRollback && !matchForNoRollback) {
            if (transactionMetadata.getExceptionHandler() != null) {
                ExceptionHandler exceptionHandler;
                if (transactionMetadata.getResource() != null) {
                    exceptionHandler = injector.getInstance(Key.get(transactionMetadata.getExceptionHandler(),
                            Names.named(transactionMetadata.getResource())));
                } else {
                    exceptionHandler = injector.getInstance(transactionMetadata.getExceptionHandler());
                }
                if (exceptionHandler != null
                        && exceptionHandler.handleException(exception,
                        new TransactionMetadata().mergeFrom(transactionMetadata),
                        currentTransaction)) {
                    transactionLogger.log("transaction exception has been handled", transactionLogger);
                } else {
                    throw exception;
                }
            } else {
                throw exception;
            }
        }
    }

    private TransactionMetadata readTransactionMetadata(MethodInvocation methodInvocation) {
        Method method = methodInvocation.getMethod();
        TransactionMetadata defaults = defaultTransactionMetadata();
        TransactionMetadata target = defaultTransactionMetadata();

        for (TransactionMetadataResolver transactionMetadataResolver : transactionMetadataResolvers) {
            target.mergeFrom(transactionMetadataResolver.resolve(methodInvocation, defaults));
        }

        Optional<Transactional> nativeTransactional = TransactionalResolver.INSTANCE.apply(method);
        if (nativeTransactional.isPresent()) {
            target.mergeFrom(nativeTransactional.get());
        } else if (TransactionPlugin.JTA_12_OPTIONAL.isPresent()) {
            Optional<javax.transaction.Transactional> transactionalOptional = JtaTransactionalResolver.INSTANCE
                    .apply(method);
            if (transactionalOptional.isPresent()) {
                javax.transaction.Transactional transactional = transactionalOptional.get();
                target.setPropagation(Propagation.valueOf(transactional.value().name()));
                if (transactional.rollbackOn().length > 0) {
                    target.setRollbackOn(asExceptions(transactional.rollbackOn()));
                }
                if (transactional.dontRollbackOn().length > 0) {
                    target.setNoRollbackFor(asExceptions(transactional.dontRollbackOn()));
                }
            }
        }

        return target;
    }

    @SuppressWarnings("unchecked")
    private TransactionMetadata defaultTransactionMetadata() {
        TransactionMetadata defaults = new TransactionMetadata();
        defaults.setPropagation(Propagation.REQUIRED);
        defaults.setReadOnly(false);
        defaults.setRollbackOnParticipationFailure(true);
        defaults.setRollbackOn(new Class[]{Exception.class});
        defaults.setNoRollbackFor(new Class[0]);
        defaults.setHandler(transactionConfig.getDefaultHandler());
        defaults.setExceptionHandler(null);
        defaults.setResource(null);
        return defaults;
    }

    private <T extends TransactionHandler<?>> T getTransactionHandler(Class<T> handlerClass, String resource) {
        if (handlerClass == null) {
            throw SeedException.createNew(TransactionErrorCode.NO_TRANSACTION_HANDLER_SPECIFIED);
        }

        try {
            if (resource == null) {
                return injector.getInstance(handlerClass);
            } else {
                return injector.getInstance(Key.get(handlerClass, Names.named(resource)));
            }
        } catch (Exception e) {
            throw SeedException.wrap(e, TransactionErrorCode.SPECIFIED_TRANSACTION_HANDLER_NOT_FOUND)
                    .put("handlerClass", handlerClass.getSimpleName())
                    .put("resource", resource == null ? "default" : resource);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Exception>[] asExceptions(Class<?>[] classes) {
        for (Class<?> aClass : classes) {
            checkArgument(Exception.class.isAssignableFrom(aClass),
                    "Class " + aClass.getName() + " is not an exception and is not supported in "
                            + "rollbackOn/noRollbackFor parameters");
        }
        return (Class<? extends Exception>[]) classes;
    }

    private final class MethodInterceptorImplementation implements MethodInterceptor {
        @Override
        @SuppressWarnings("unchecked")
        public Object invoke(MethodInvocation invocation) throws Throwable {
            TransactionLogger transactionLogger = new TransactionLogger();
            transactionLogger.log("intercepting {}#{}", invocation.getMethod().getDeclaringClass().getCanonicalName(),
                    invocation.getMethod().getName());

            TransactionMetadata transactionMetadata = readTransactionMetadata(invocation);
            transactionLogger.log("{}", transactionMetadata);

            TransactionHandler<?> transactionHandler;
            try {
                transactionHandler = getTransactionHandler(transactionMetadata.getHandler(),
                        transactionMetadata.getResource());
            } catch (SeedException e) {
                throw e.put("method", invocation.getMethod().toString());
            }

            transactionLogger.log("using {} transaction handler", transactionHandler.getClass().getCanonicalName());

            try {
                return doMethodInterception(transactionLogger, invocation, transactionMetadata, transactionHandler);
            } catch (SeedException e) {
                throw e.put("method", invocation.getMethod().toString());
            }
        }
    }
}
