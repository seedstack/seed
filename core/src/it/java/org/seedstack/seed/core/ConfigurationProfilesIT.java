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
import net.jcip.annotations.NotThreadSafe;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.Application;

import javax.inject.Inject;

@NotThreadSafe
public class ConfigurationProfilesIT {
    static class Holder {
        @Inject
        Application application;
    }

    @Test
    public void zero_configuration_profile_can_be_applied() {
        Kernel kernel = Seed.createKernel();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("base-value");
        } finally {
            Seed.disposeKernel(kernel);
        }

        System.setProperty("org.seedstack.seed.profiles", "");

        kernel = Seed.createKernel();
        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("base-value");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
            Seed.disposeKernel(kernel);
        }
    }

    @Test
    public void one_configuration_profile_can_be_applied() {
        System.setProperty("org.seedstack.seed.profiles", "dev");

        Kernel kernel = Seed.createKernel();
        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("dev-value");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
            Seed.disposeKernel(kernel);
        }

        System.setProperty("org.seedstack.seed.profiles", "preprod");

        kernel = Seed.createKernel();
        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("preprod-value");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
            Seed.disposeKernel(kernel);
        }

        System.setProperty("org.seedstack.seed.profiles", "prod");

        kernel = Seed.createKernel();
        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("prod-value");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
            Seed.disposeKernel(kernel);
        }
    }

    @Test
    public void multiple_configuration_profiles_can_be_applied() {
        System.setProperty("org.seedstack.seed.profiles", "dev");

        Kernel kernel = Seed.createKernel();
        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("dev-value");
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.debug-mode")).isEqualTo("off");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
            Seed.disposeKernel(kernel);
        }

        System.setProperty("org.seedstack.seed.profiles", "dev,debug");

        kernel = Seed.createKernel();
        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("dev-value");
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.debug-mode")).isEqualTo("on");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
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
