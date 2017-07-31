/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import com.google.inject.Injector;
import com.google.inject.Module;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.seedstack.seed.Ignore;
import org.seedstack.seed.LifecycleListener;
import org.seedstack.seed.core.rules.SeedITRule;

import javax.inject.Singleton;

import static org.assertj.core.api.Assertions.assertThat;

public class LifecycleIT implements LifecycleListener {
    @Rule
    public SeedITRule rule = new SeedITRule(this);
    private static boolean startedWasCalled;
    private static boolean stoppingWasCalled;
    private static boolean closedWasCalled;
    private static boolean ignoredClosedWasCalled;

    @BeforeClass
    public static void setUpClass() throws Exception {
        startedWasCalled = false;
        stoppingWasCalled = false;
        closedWasCalled = false;
    }

    @Before
    public void setUp() throws Exception {
        rule.getKernel().objectGraph().as(Injector.class).createChildInjector((Module) binder -> binder.bind(AutoCloseableFixture.class));
        assertThat(startedWasCalled).isTrue();
        assertThat(stoppingWasCalled).isFalse();
        assertThat(closedWasCalled).isFalse();
        assertThat(ignoredClosedWasCalled).isFalse();
    }

    @After
    public void tearDown() throws Exception {
        assertThat(startedWasCalled).isTrue();
        assertThat(stoppingWasCalled).isFalse();
        assertThat(closedWasCalled).isFalse();
        assertThat(ignoredClosedWasCalled).isFalse();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        assertThat(startedWasCalled).isTrue();
        assertThat(stoppingWasCalled).isTrue();
        assertThat(closedWasCalled).isTrue();
        assertThat(ignoredClosedWasCalled).isFalse();
    }

    @Test
    public void lifecycle_callbacks_are_invoked() {
        assertThat(startedWasCalled).isTrue();
        assertThat(stoppingWasCalled).isFalse();
        assertThat(closedWasCalled).isFalse();
        assertThat(ignoredClosedWasCalled).isFalse();
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
        public void close() throws Exception {
            closedWasCalled = true;
        }
    }

    @Singleton
    @Ignore
    private static class IgnoredAutoCloseableFixture implements AutoCloseable {
        @Override
        public void close() throws Exception {
            ignoredClosedWasCalled = true;
        }
    }
}
