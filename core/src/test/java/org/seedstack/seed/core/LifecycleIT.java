/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import com.google.inject.Injector;
import com.google.inject.name.Names;
import io.nuun.kernel.api.Kernel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seedstack.seed.Ignore;
import org.seedstack.seed.LifecycleListener;
import org.seedstack.seed.core.internal.transaction.TransactionalClassProxy;
import org.seedstack.seed.core.internal.transaction.TransactionalProxy;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Priority;
import javax.inject.Singleton;

import static org.assertj.core.api.Assertions.assertThat;

@Priority(5)
public class LifecycleIT implements LifecycleListener {
    private static boolean startedWasCalled;
    private static boolean stoppingWasCalled;
    private static boolean closedWasCalled;
    private static boolean ignoredClosedWasCalled;
    private static boolean proxyClosedWasCalled;
    private static boolean classProxyClosedWasCalled;
    private static boolean preDestroySingletonCalled;
    private static boolean preDestroyCalled;
    private static boolean postConstructSingletonCalled;
    private static boolean postConstructCalled;
    private Kernel kernel;
    private Injector childInjector;

    @BeforeClass
    public static void setUpClass() {
        startedWasCalled = false;
        stoppingWasCalled = false;
        closedWasCalled = false;
    }

    @AfterClass
    public static void tearDownClass() {
        assertThat(startedWasCalled).isTrue();
        assertThat(stoppingWasCalled).isTrue();
        assertThat(closedWasCalled).isTrue();
        assertThat(ignoredClosedWasCalled).isFalse();
        assertThat(preDestroySingletonCalled).isTrue();
    }

    @Before
    public void setUp() {
        kernel = Seed.createKernel();
        childInjector = kernel.objectGraph().as(Injector.class).createChildInjector(binder -> {
            binder.bind(PreDestroyFixture.class);
            binder.bind(StatelessFixture.class);
            binder.bind(SingletonFixture.class);
            binder.bind(AutoCloseableFixture.class);
            binder.bind(IgnoredAutoCloseableFixture.class);
            binder.bind(AutoCloseable.class)
                    .annotatedWith(Names.named("proxy"))
                    .toInstance(TransactionalProxy.create(AutoCloseable.class, ProxyAutoCloseableFixture::new));
            binder.bind(AutoCloseable.class)
                    .annotatedWith(Names.named("classProxy"))
                    .toInstance(TransactionalClassProxy.create(ClassProxyAutoCloseableFixture.class,
                            ClassProxyAutoCloseableFixture::new));
        });
        assertThat(startedWasCalled).isTrue();
        assertThat(stoppingWasCalled).isFalse();
        assertThat(closedWasCalled).isFalse();
        assertThat(ignoredClosedWasCalled).isFalse();
        assertThat(postConstructSingletonCalled).isTrue();
        assertThat(preDestroySingletonCalled).isFalse();
    }

    @After
    public void tearDown() {
        assertThat(startedWasCalled).isTrue();
        assertThat(stoppingWasCalled).isFalse();
        assertThat(closedWasCalled).isFalse();
        assertThat(ignoredClosedWasCalled).isFalse();
        assertThat(preDestroySingletonCalled).isFalse();
        Seed.disposeKernel(kernel);
    }

    @Test
    public void lifecycle_callbacks_are_invoked() {
        assertThat(startedWasCalled).isTrue();
        assertThat(stoppingWasCalled).isFalse();
        assertThat(closedWasCalled).isFalse();
        assertThat(ignoredClosedWasCalled).isFalse();
        assertThat(proxyClosedWasCalled).isFalse();
        assertThat(classProxyClosedWasCalled).isFalse();
        assertThat(postConstructSingletonCalled).isTrue();

        assertThat(postConstructCalled).isFalse();
        assertThat(preDestroyCalled).isFalse();
        childInjector.getInstance(StatelessFixture.class);
        assertThat(postConstructCalled).isTrue();
        assertThat(preDestroyCalled).isFalse();

        assertThat(preDestroySingletonCalled).isFalse();
    }

    @Override
    public void started() {
        startedWasCalled = true;
    }

    @Override
    public void stopping() {
        stoppingWasCalled = true;
    }

    @Singleton
    private static class AutoCloseableFixture implements AutoCloseable {
        @Override
        public void close() {
            closedWasCalled = true;
        }
    }

    @Singleton
    private static class IgnoredAutoCloseableFixture implements AutoCloseable {
        @Override
        @Ignore
        public void close() {
            ignoredClosedWasCalled = true;
        }
    }

    @Singleton
    private static class ProxyAutoCloseableFixture implements AutoCloseable {
        @Override
        public void close() {
            proxyClosedWasCalled = true;
        }
    }

    @Singleton
    private static class ClassProxyAutoCloseableFixture implements AutoCloseable {
        ClassProxyAutoCloseableFixture() {
        }

        @Override
        public void close() {
            classProxyClosedWasCalled = true;
        }
    }

    @Singleton
    private static class PreDestroyFixture {
    }

    @Singleton
    private static class SingletonFixture {
        @PostConstruct
        public void postConstruct() {
            postConstructSingletonCalled = true;
        }

        @PreDestroy
        public void preDestroy() {
            preDestroySingletonCalled = true;
        }
    }

    private static class StatelessFixture {
        @PostConstruct
        public void postConstruct() {
            postConstructCalled = true;
        }

        @PreDestroy
        public void preDestroy() {
            preDestroyCalled = true;
        }
    }
}
