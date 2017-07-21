/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it;

import com.google.inject.Injector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Application;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.it.fixtures.ITBound;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestingITRunner.class)
public class SeedITRunnerIT {
    @Inject
    Injector injector;

    @Configuration("custom-property")
    private String customConfiguration;

    static boolean passedBeforeClass = false;
    static boolean passedAfterClass = false;
    static boolean passedBeforeKernel = false;
    static boolean passedAfterKernel = false;

    @BeforeKernel
    public static void beforeKernel() {
        assertThat(passedBeforeKernel).isFalse();
        assertThat(passedBeforeClass).isFalse();
        assertThat(passedAfterClass).isFalse();
        assertThat(passedAfterKernel).isFalse();
        passedBeforeKernel = true;
    }

    @BeforeClass
    public static void beforeClass() {
        assertThat(passedBeforeKernel).isTrue();
        assertThat(passedBeforeClass).isFalse();
        assertThat(passedAfterClass).isFalse();
        assertThat(passedAfterKernel).isFalse();
        passedBeforeClass = true;
    }

    @AfterClass
    public static void afterClass() {
        assertThat(passedBeforeKernel).isTrue();
        assertThat(passedBeforeClass).isTrue();
        assertThat(passedAfterClass).isFalse();
        assertThat(passedAfterKernel).isFalse();
        passedAfterClass = true;
    }

    @AfterKernel
    public static void afterKernel() {
        assertThat(passedBeforeKernel).isTrue();
        assertThat(passedBeforeClass).isTrue();
        assertThat(passedAfterClass).isTrue();
        assertThat(passedAfterKernel).isFalse();
        passedAfterKernel = true;
    }

    @Test
    public void seed_it_runner_is_injecting_test_class_properly() {
        assertThat(passedBeforeKernel).isTrue();
        assertThat(passedBeforeClass).isTrue();
        assertThat(passedAfterClass).isFalse();
        assertThat(passedAfterKernel).isFalse();
        assertThat(injector).isNotNull();
    }

    @Test
    public void seed_it_runner_is_correctly_passing_default_configuration() {
        assertThat(injector.getInstance(Application.class).getConfiguration().getMandatory(String.class, "testKey")).isEqualTo("testValue");
    }

    @Test
    public void it_bound_classes_are_bound() {
        assertThat(injector.getInstance(ITBound.class)).isNotNull();
    }

    @Test
    public void configuration_is_injected() {
        assertThat(customConfiguration).isEqualTo("custom-value");
    }
}
