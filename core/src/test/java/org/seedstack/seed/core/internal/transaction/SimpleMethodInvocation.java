/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.transaction;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInvocation;

public class SimpleMethodInvocation implements MethodInvocation {
    private Object self;
    private Method method;
    private Object[] arguments;

    public SimpleMethodInvocation(Object self, Method method, Object[] arguments) {
        this.self = self;
        this.method = method;
        this.arguments = arguments;
    }

    public Object[] getArguments() {
        return arguments;
    }

    void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public Method getMethod() {
        return method;
    }

    public AccessibleObject getStaticPart() {
        throw new UnsupportedOperationException("mock method not implemented");
    }

    public Object getThis() {
        return self;
    }

    public Object proceed() throws Throwable {
        try {
            return method.invoke(self, arguments);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                throw e.getCause();
            } else {
                throw e;
            }
        }
    }
}