/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.el;

import com.google.inject.Injector;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import javax.inject.Inject;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.el.ELContextBuilder;
import org.seedstack.seed.el.ELService;
import org.seedstack.seed.el.spi.ELHandler;

class ELInterceptor implements MethodInterceptor {

    private Class<? extends Annotation> annotationClass;

    private ELBinder.ExecutionPolicy policy;

    // Get a map of annotation handler
    @Inject
    private Map<Class<? extends Annotation>, Class<ELHandler>> elMap;

    @Inject
    private ELService elService;

    @Inject
    private ELContextBuilder elContextBuilder;

    @Inject
    private Injector injector;

    ELInterceptor(Class<? extends Annotation> annotationClass, ELBinder.ExecutionPolicy policy) {
        this.annotationClass = annotationClass;
        this.policy = policy;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Class<ELHandler> handlerClass = elMap.get(this.annotationClass);
        ELHandler ELHandler = injector.getInstance(handlerClass);

        // The policy defines if the EL is evaluated before the method, after or both.

        if (ELBinder.ExecutionPolicy.BEFORE.equals(policy) || ELBinder.ExecutionPolicy.BOTH.equals(policy)) {
            ELHandler.handle(evaluateELWithService(null, invocation));
        }

        Object obj = null;
        try {
            obj = invocation.proceed();

        } finally {
            if (ELBinder.ExecutionPolicy.AFTER.equals(policy) || ELBinder.ExecutionPolicy.BOTH.equals(policy)) {
                ELHandler.handle(evaluateELWithService(obj, invocation));
            }
        }

        return obj;
    }

    private Object evaluateELWithService(Object obj, MethodInvocation invocation) {
        return elService.withExpression(getELFromAnnotation(invocation.getMethod()), Object.class)
                .withContext(elContextBuilder.defaultContext().withProperty("result", obj).withProperty("args",
                        invocation.getArguments()).build())
                .asValueExpression().eval();
    }

    private String getELFromAnnotation(Method method) {
        String el;

        try {
            Method methodValue = this.annotationClass.getMethod("value");
            el = (String) methodValue.invoke(method.getAnnotation(this.annotationClass));

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw SeedException.wrap(e, ExpressionLanguageErrorCode.NO_METHOD_VALUE_AVAILABLE).put("annotation",
                    this.annotationClass);
        }

        return el;
    }
}
