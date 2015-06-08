/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.seedstack.seed.core.api.Logging;
import io.nuun.kernel.api.Kernel;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seedstack.seed.core.fixtures.Service1;
import org.seedstack.seed.core.fixtures.Service2;
import org.seedstack.seed.core.fixtures.Service3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import some.different.pkg.AnotherForeignClass;
import some.other.pkg.ForeignClass;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static io.nuun.kernel.core.NuunCore.createKernel;
import static io.nuun.kernel.core.NuunCore.newKernelConfiguration;

public class CorePluginIT {
    Injector injector;
    static Kernel underTest;

    @BeforeClass
    public static void beforeClass() {
        underTest = createKernel(newKernelConfiguration());
        underTest.init();
        underTest.start();
    }

    @AfterClass
    public static void afterClass() {
        underTest.stop();
    }

    static class LoggerHolder {
        private static final Logger logger = LoggerFactory.getLogger(LoggerHolder.class);

        @Logging
        private Logger logger1;
    }

    static class HolderNominal {
        @Inject
        Service1 s1;
        @Inject
        @Nullable
        Service3 s3;
        @Inject
        ForeignClass foreignClass;
        @Inject
        AnotherForeignClass anotherForeignClass;
    }

    static class HolderException {
        @Inject
        Service2 s2;
    }

    @Before
    public void before() {

        Module aggregationModule = new AbstractModule() {

            @Override
            protected void configure() {
                bind(HolderNominal.class);
                bind(LoggerHolder.class);
            }
        };
        injector = underTest.objectGraph().as(Injector.class).createChildInjector(
                aggregationModule);
    }

    @Test
    public void modules_are_installed_correctly() {
        HolderNominal holder = injector.getInstance(HolderNominal.class);

        Assertions.assertThat(holder).isNotNull();
        Assertions.assertThat(holder.s1).isNotNull();
        Assertions.assertThat(holder.s3).isNull();
    }

    @Test(expected = ConfigurationException.class)
    public void modules_without_install_are_not_installed_correctly() {
        injector.getInstance(HolderException.class);
    }

    @Test
    public void logger_injection_is_working() {
        LoggerHolder holder = injector.getInstance(LoggerHolder.class);

        Assertions.assertThat(LoggerHolder.logger).isNotNull();
        Assertions.assertThat(holder.logger1).isSameAs(LoggerHolder.logger);
    }

    @Test
    public void multiple_package_roots_can_be_used() {
        HolderNominal holder = injector.getInstance(HolderNominal.class);

        Assertions.assertThat(holder.foreignClass).isNotNull();
        Assertions.assertThat(holder.anotherForeignClass).isNotNull();
    }
}
