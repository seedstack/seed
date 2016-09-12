/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import io.nuun.kernel.api.Kernel;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.coffig.ConfigurationException;
import org.seedstack.seed.Application;
import org.seedstack.seed.ApplicationConfig;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.core.fixtures.SomeEnum;

import javax.inject.Inject;

public class ConfigurationIT {
    private static final String CONSTANT_TEST = "this is a test";

    static class Holder {
        @Inject
        Application application;

        @Configuration("secret1")
        String secret1;

        @Configuration(value = "dummy", defaultValue = "defaultValue")
        String dummy;

        @Configuration("someEnum")
        SomeEnum someEnum;

        @Configuration("anInt")
        int anInt;

        @Configuration("someShorts")
        short[] someShorts;
    }

    @Test
    public void configuration_injection_is_working_correctly() {
        Kernel kernel = Seed.createKernel();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder).isNotNull();
            Assertions.assertThat(holder.application).isNotNull();
            Assertions.assertThat(holder.secret1).isNotNull().isEqualTo("**I am Alice**");
            Assertions.assertThat(holder.dummy).isNotNull().isEqualTo("defaultValue");
            Assertions.assertThat(holder.anInt).isNotNull().isEqualTo(5);
            Assertions.assertThat(holder.someShorts).isNotEmpty().isEqualTo(new short[]{2, 3, 4});
            Assertions.assertThat(holder.someEnum).isNotNull().isEqualTo(SomeEnum.FOO);
        } finally {
            Seed.disposeKernel(kernel);
        }
    }

    @Test
    public void configuration_can_be_retrieved() {
        Kernel kernel = Seed.createKernel();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().get(ApplicationConfig.class).getId()).isEqualTo("seed-it");
        } finally {
            Seed.disposeKernel(kernel);
        }
    }

    @Test
    public void system_properties_are_accessible_in_configuration() {
        Kernel kernel = Seed.createKernel();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().get(String.class, "sys.java\\.vendor")).isEqualTo(System.getProperty("java.vendor"));
        } finally {
            Seed.disposeKernel(kernel);
        }
    }

    @Test
    public void environment_variables_are_accessible_in_configuration() {
        Kernel kernel = Seed.createKernel();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().get(String.class, "env.JAVA_HOME")).isEqualTo(System.getenv("JAVA_HOME"));
        } finally {
            Seed.disposeKernel(kernel);
        }
    }

    @Test
    public void empty_configuration_values_yield_empty_string() {
        Kernel kernel = Seed.createKernel();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getMandatory(String.class, "empty")).isEqualTo("");
        } finally {
            Seed.disposeKernel(kernel);
        }
    }

    @Test(expected = ConfigurationException.class)
    public void non_existent_configuration_values_throws_exception() {
        Kernel kernel = Seed.createKernel();

        try {
            Holder holder = getHolder(kernel);
            holder.application.getConfiguration().getMandatory(String.class, "nonExistent");
        } finally {
            Seed.disposeKernel(kernel);
        }
    }

    private Holder getHolder(Kernel kernel) {
        return kernel.objectGraph().as(Injector.class).createChildInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Holder.class);
            }
        }).getInstance(Holder.class);
    }
}
