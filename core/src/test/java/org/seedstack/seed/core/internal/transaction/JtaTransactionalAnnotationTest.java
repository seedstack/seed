/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.seedstack.shed.reflect.ReflectUtils.invoke;
import static org.seedstack.shed.reflect.ReflectUtils.makeAccessible;

import com.google.inject.matcher.Matcher;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import javax.transaction.Transactional;
import org.junit.Before;
import org.junit.Test;

public class JtaTransactionalAnnotationTest {
    private Matcher<Method> methodMatcher;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        methodMatcher = invoke(makeAccessible(TransactionModule.class.getDeclaredMethod("buildMethodMatcher")), null);
    }

    @Test
    public void metaAnnotation() throws Exception {
        assertThat(methodMatcher.matches(MetaTransactionalClass.class.getMethod("someMethod"))).isTrue();
        assertThat(methodMatcher.matches(NotTransactionalClass.class.getMethod("metaTransactionalMethod"))).isTrue();
    }

    @Test
    public void annotationOnMethod() throws Exception {
        assertThat(methodMatcher.matches(NotTransactionalClass.class.getMethod("transactionalMethod"))).isTrue();
        assertThat(methodMatcher.matches(NotTransactionalInterface.class.getMethod("transactionalMethod"))).isTrue();
    }

    @Test
    public void annotationOnClass() throws Exception {
        assertThat(methodMatcher.matches(TransactionalClass.class.getMethod("someMethod"))).isTrue();
        assertThat(methodMatcher.matches(TransactionalInterface.class.getMethod("someMethod"))).isTrue();
    }

    @Test
    public void annotationOnInheritedInterfaceMethod() throws Exception {
        assertThat(methodMatcher.matches(InterfaceMethodInherited.class.getMethod("transactionalMethod"))).isTrue();
        assertThat(methodMatcher.matches(InterfaceMethodInherited.class.getMethod("localMethod"))).isFalse();
    }

    @Test
    public void annotationOnInheritedInterface() throws Exception {
        assertThat(methodMatcher.matches(InterfaceInherited.class.getMethod("someMethod"))).isTrue();
        assertThat(methodMatcher.matches(InterfaceInherited.class.getMethod("localMethod"))).isFalse();
    }

    @Test
    public void annotationOnInheritedClassMethod() throws Exception {
        assertThat(methodMatcher.matches(ClassMethodInherited.class.getMethod("transactionalMethod"))).isTrue();
        assertThat(methodMatcher.matches(ClassMethodInherited.class.getMethod("localMethod"))).isFalse();
    }

    @Test
    public void annotationOnInheritedClass() throws Exception {
        assertThat(methodMatcher.matches(ClassInherited.class.getMethod("someMethod"))).isTrue();
        assertThat(methodMatcher.matches(ClassInherited.class.getMethod("localMethod"))).isTrue();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Transactional
    private @interface MetaTransactional {
    }

    @Transactional
    interface TransactionalInterface {
        void someMethod();
    }

    interface NotTransactionalInterface {
        @Transactional
        void transactionalMethod();
    }

    @MetaTransactional
    static class MetaTransactionalClass {
        public void someMethod() {

        }
    }

    @Transactional
    static class TransactionalClass {
        public void someMethod() {

        }
    }

    static class NotTransactionalClass {
        @Transactional
        public void transactionalMethod() {

        }

        @MetaTransactional
        public void metaTransactionalMethod() {

        }
    }

    static class InterfaceMethodInherited implements NotTransactionalInterface {
        @Override
        public void transactionalMethod() {

        }

        public void localMethod() {

        }
    }

    static class InterfaceInherited implements TransactionalInterface {
        @Override
        public void someMethod() {

        }

        public void localMethod() {

        }
    }

    static class ClassMethodInherited extends NotTransactionalClass {
        public void transactionalMethod() {

        }

        public void localMethod() {

        }
    }

    static class ClassInherited extends TransactionalClass {
        public void someMethod() {

        }

        public void localMethod() {

        }
    }
}
