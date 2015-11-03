/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
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

import static io.nuun.kernel.core.NuunCore.createKernel;
import static io.nuun.kernel.core.NuunCore.newKernelConfiguration;

@NotThreadSafe
public class ConfigurationProfilesIT {
    static class Holder {
        @Inject
        Application application;
    }

    @Test
    public void zero_configuration_profile_can_be_applied() {
        Kernel kernel = setup();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("base-value");
        } finally {
            teardown(kernel);
        }

        System.setProperty("org.seedstack.seed.profiles", "");

        kernel = setup();
        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("base-value");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
            teardown(kernel);
        }
    }

    @Test
    public void one_configuration_profile_can_be_applied() {
        System.setProperty("org.seedstack.seed.profiles", "dev");

        Kernel kernel = setup();
        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("dev-value");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
            teardown(kernel);
        }

        System.setProperty("org.seedstack.seed.profiles", "preprod");

        kernel = setup();
        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("preprod-value");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
            teardown(kernel);
        }

        System.setProperty("org.seedstack.seed.profiles", "prod");

        kernel = setup();
        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("prod-value");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
            teardown(kernel);
        }
    }

    @Test
    public void multiple_configuration_profiles_can_be_applied() {
        System.setProperty("org.seedstack.seed.profiles", "dev");

        Kernel kernel = setup();
        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("dev-value");
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.debug-mode")).isEqualTo("off");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
            teardown(kernel);
        }

        System.setProperty("org.seedstack.seed.profiles", "dev,debug");

        kernel = setup();
        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("dev-value");
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.debug-mode")).isEqualTo("on");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
            teardown(kernel);
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


    private Kernel setup() {
        Kernel kernel = createKernel(newKernelConfiguration());
        kernel.init();
        kernel.start();

        return kernel;
    }

    private void teardown(Kernel kernel) {
        kernel.stop();
    }
}
