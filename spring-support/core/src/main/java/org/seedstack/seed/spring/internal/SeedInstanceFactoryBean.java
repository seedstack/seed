/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.spring.internal;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.springframework.beans.factory.FactoryBean;

import javax.inject.Inject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static com.google.common.base.Preconditions.checkNotNull;

class SeedInstanceFactoryBean implements FactoryBean<Object> {
    @Inject
    private static Injector injector;

    private String classname;
    private String qualifier;
    private boolean proxy = true;

    private static Object createInstance(Class<?> instanceClass, String qualifier) {
        if (qualifier == null) {
            return injector.getInstance(instanceClass);
        } else {
            return injector.getInstance(Key.get(instanceClass, Names.named(qualifier)));
        }
    }

    private static final class InstanceProxy implements InvocationHandler {
        private static final Method OBJECT_EQUALS = getObjectMethod("equals", Object.class);

        private final Class<?> instanceClass;
        private final String qualifier;

        private volatile Object instance;

        private InstanceProxy(Class instanceClass, String qualifier) {
            checkNotNull(instanceClass);
            this.instanceClass = instanceClass;
            this.qualifier = qualifier;
        }

        private void initialize() {
            // double checked locking only work if volatile modifier is applied on instance reference
            // see http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
            if (instance == null) {
                synchronized (this) {
                    if (instance == null) {
                        instance = createInstance(instanceClass, qualifier);
                    }
                }
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            initialize();

            if (OBJECT_EQUALS.equals(method)) {
                return equalsInternal(args[0]);
            }

            try {
                return method.invoke(instance, args);
            } catch (InvocationTargetException e) { // NOSONAR
                throw e.getCause();
            }
        }

        private boolean equalsInternal(Object other) { // NOSONAR
            // same proxy <==> same underlying object
            if (this == other) {
                return true;
            }

            if (Proxy.isProxyClass(other.getClass())) {
                InvocationHandler handler = Proxy.getInvocationHandler(other);
                if (handler instanceof InstanceProxy) {
                    ((InstanceProxy) handler).initialize();
                    return ((InstanceProxy) handler).instance.equals(instance);
                } else {
                    return false;
                }
            } else {
                return instance.equals(other);
            }
        }

        private static Method getObjectMethod(String name, Class... types) {
            try {
                // null 'types' is OK.
                return Object.class.getMethod(name, types);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    @Override
    public Object getObject() throws Exception {
        if (classname == null) {
            throw new IllegalArgumentException("Property classname is required for SeedFactoryBean");
        } else {
            Class<?> instanceClass = Class.forName(classname);

            if (proxy) {
                // delay underlying instance retrieving via proxy to break circular dependencies problems
                return Proxy.newProxyInstance(SeedInstanceFactoryBean.class.getClassLoader(), new Class<?>[]{instanceClass}, new InstanceProxy(instanceClass, qualifier));
            } else {
                return createInstance(instanceClass, qualifier);
            }
        }
    }

    @Override
    public Class<?> getObjectType() {
        if (classname == null) {
            return null;
        }

        try {
            return Class.forName(classname);
        } catch (ClassNotFoundException e) { // NOSONAR
            return null;
        }
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public boolean isProxy() {
        return proxy;
    }

    public void setProxy(boolean proxy) {
        this.proxy = proxy;
    }
}
