/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.transaction.internal;

import com.google.common.base.Predicate;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.reflections.ReflectionUtils;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.transaction.Transactional;
import org.seedstack.seed.transaction.spi.ExceptionHandler;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionManager;
import org.seedstack.seed.transaction.spi.TransactionMetadata;
import org.seedstack.seed.transaction.spi.TransactionMetadataResolver;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Base class for common transaction manager behavior.
 *
 * @author adrien.lauer@mpsa.com
 */
public abstract class AbstractTransactionManager implements TransactionManager {
    private final MethodInterceptorImplementation methodInterceptorImplementation = new MethodInterceptorImplementation();
    @Inject
    protected Injector injector;
    @Inject
    private Set<TransactionMetadataResolver> transactionMetadataResolvers;

    private final class MethodInterceptorImplementation implements MethodInterceptor {
        @Override
        @SuppressWarnings("unchecked")
        public Object invoke(MethodInvocation invocation) throws Throwable {
            TransactionLogger transactionLogger = new TransactionLogger();
            transactionLogger.log("intercepting {}#{}", invocation.getMethod().getDeclaringClass().getCanonicalName(), invocation.getMethod().getName());

            TransactionMetadata transactionMetadata = readTransactionMetadata(invocation);
            transactionLogger.log("{}", transactionMetadata);

            TransactionHandler<Object> transactionHandler;
            try {
                transactionHandler = getTransactionHandler(transactionMetadata.getHandler(), transactionMetadata.getResource());
            } catch (SeedException e) {
                throw e.put("method", invocation.getMethod().toString());
            }

            transactionLogger.log("using {} transaction handler", transactionHandler.getClass().getCanonicalName());

            return doMethodInterception(transactionLogger, invocation, transactionMetadata, transactionHandler);
        }
    }

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
    protected abstract Object doMethodInterception(TransactionLogger transactionLogger, MethodInvocation invocation, TransactionMetadata transactionMetadata, TransactionHandler<Object> transactionHandler) throws Throwable;

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
    protected Object doInvocation(TransactionLogger transactionLogger, MethodInvocation invocation, TransactionMetadata transactionMetadata, Object currentTransaction) throws Throwable {
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
    private void doHandleException(TransactionLogger transactionLogger, Exception exception, TransactionMetadata transactionMetadata, Object currentTransaction) throws Exception {
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
                    exceptionHandler = injector.getInstance(Key.get(transactionMetadata.getExceptionHandler(), Names.named(transactionMetadata.getResource())));
                } else {
                    exceptionHandler = injector.getInstance(transactionMetadata.getExceptionHandler());
                }
                if (exceptionHandler != null && exceptionHandler.handleException(exception, new TransactionMetadata().mergeFrom(transactionMetadata), currentTransaction)) {
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
        Class<?> targetClass = SeedReflectionUtils.cleanProxy(methodInvocation.getThis().getClass());
        TransactionMetadata transactionMetadataDefaults = injector.getInstance(TransactionMetadata.class).defaults();
        TransactionMetadata transactionMetadata = injector.getInstance(TransactionMetadata.class).defaults();

        for (TransactionMetadataResolver transactionMetadataResolver : transactionMetadataResolvers) {
            transactionMetadata.mergeFrom(transactionMetadataResolver.resolve(methodInvocation, transactionMetadataDefaults));
        }

        transactionMetadata.mergeFrom(deepGetAnnotation(method, targetClass));

        return transactionMetadata;
    }


    private <T extends TransactionHandler> T getTransactionHandler(Class<T> handlerClass, String resource) {
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
    private Transactional deepGetAnnotation(final Method method, Class<?> targetClass) {
        Transactional transaction = method.getAnnotation(Transactional.class);

        // Fetching annotations from method (we fetch all methods in super and interfaces)
        if (transaction == null) {
            Predicate<? super Method> predicate = (Predicate<Method>) input -> (!method.equals(input)) && methodIsEqual(method, input);

            Set<Method> methods = ReflectionUtils.getAllMethods(targetClass, predicate);

            for (Method method2 : methods) {
                transaction = method2.getAnnotation(Transactional.class);
                if (transaction != null) {
                    break;
                }
            }
        }

        // Fetching annotation from class
        if (transaction == null) {
            transaction = SeedReflectionUtils.getMetaAnnotationFromAncestors(targetClass, Transactional.class);
        }

        return transaction;
    }

    private boolean methodIsEqual(Method left, Method right) {
        EqualsBuilder builder = new EqualsBuilder()
                .append(left.getName(), right.getName())
                .append(left.getParameterTypes(), right.getParameterTypes())
                .append(left.getReturnType(), right.getReturnType());

        return builder.isEquals();
    }
}
