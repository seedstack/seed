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
import com.google.inject.Injector;
import com.google.inject.Module;
import io.nuun.kernel.api.Kernel;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.api.Configuration;
import org.seedstack.seed.core.api.Logging;
import org.seedstack.seed.core.fixtures.SomeEnum;
import org.seedstack.seed.core.internal.sample.MyClass;
import org.slf4j.Logger;

import javax.inject.Inject;

import java.util.Map;

import static io.nuun.kernel.core.NuunCore.createKernel;
import static io.nuun.kernel.core.NuunCore.newKernelConfiguration;

public class SeedApplicationPluginIT {
    public static final String CONSTANT_TEST = "this is a test";

    static Kernel underTest;
    Holder holder;

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

        @Logging
        Logger logger;
    }

    @Test
    public void application_injection_is_working_correctly() {
        setup();

        try {
            Assertions.assertThat(holder).isNotNull();
            Assertions.assertThat(holder.application).isNotNull();
            Assertions.assertThat(holder.secret1).isNotNull().isEqualTo("**I am Alice**");
            Assertions.assertThat(holder.dummy).isNotNull().isEqualTo("defaultValue");
            Assertions.assertThat(holder.anInt).isNotNull().isEqualTo(5);
            Assertions.assertThat(holder.someShorts).isNotEmpty().isEqualTo(new short[]{2, 3, 4});
            Assertions.assertThat(holder.someEnum).isNotNull().isEqualTo(SomeEnum.FOO);
            Assertions.assertThat(holder.logger).isNotNull();
            Assertions.assertThat(holder.logger.getName()).isEqualTo("org.seedstack.seed.core.SeedApplicationPluginIT$Holder");
        } finally {
            teardown();
        }
    }

    @Test
    public void injector_graphing_is_working_correctly() throws Exception {
        setup();

        try {
            String injectorGraph = holder.application.getInjectionGraph(null);

            Assertions.assertThat(injectorGraph).isNotNull();
            Assertions.assertThat(injectorGraph).isNotEmpty();
        } finally {
            teardown();
        }
    }

    @Test
    public void configuration_can_be_retrieved() {
        setup();

        try {
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.core.application-id")).isEqualTo("seed-it");
        } finally {
            teardown();
        }
    }

    @Test
    public void zero_configuration_profile_can_be_applied() {
        setup();
        try {
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("base-value");
        } finally {
            teardown();
        }

        System.setProperty("org.seedstack.seed.profiles", "");

        setup();
        try {
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("base-value");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
            teardown();
        }
    }

    @Test
    public void one_configuration_profile_can_be_applied() {
        System.setProperty("org.seedstack.seed.profiles", "dev");

        setup();
        try {
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("dev-value");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
            teardown();
        }

        System.setProperty("org.seedstack.seed.profiles", "preprod");

        setup();
        try {
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("preprod-value");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
            teardown();
        }

        System.setProperty("org.seedstack.seed.profiles", "prod");

        setup();
        try {
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("prod-value");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
            teardown();
        }
    }

    @Test
    public void multiple_configuration_profiles_can_be_applied() {
        System.setProperty("org.seedstack.seed.profiles", "dev");

        setup();
        try {
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("dev-value");
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.debug-mode")).isEqualTo("off");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
            teardown();
        }

        System.setProperty("org.seedstack.seed.profiles", "dev,debug");

        setup();
        try {
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.test-property")).isEqualTo("dev-value");
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.test.debug-mode")).isEqualTo("on");
        } finally {
            System.clearProperty("org.seedstack.seed.profiles");
            teardown();
        }
    }

    @Test
    public void only_metainf_configuration_is_considered_for_configuration() {
        setup();
        Assertions.assertThat(holder.application.getConfiguration().getString("root-properties-test")).isNull();
        Assertions.assertThat(holder.application.getConfiguration().getString("root-props-test")).isNull();
        teardown();
    }

    @Test
    public void advanced_configuration_usage_is_working() {
        setup();

        try {
            Assertions.assertThat(holder.application.getConfiguration().getString("root-property")).isEqualTo("the-root-value");
            Assertions.assertThat(holder.application.getConfiguration().getString("secret1")).isEqualTo("**I am Alice**");
            Assertions.assertThat(holder.application.getConfiguration().getString("secret2")).isEqualTo("**I am Bob**");
        } finally {
            teardown();
        }
    }

    @Test
    public void properties_and_props_are_loaded() {
        setup();

        try {
            Assertions.assertThat(holder.application.getConfiguration().getString("any-file-property-1")).isEqualTo("the-value-1");
            Assertions.assertThat(holder.application.getConfiguration().getString("any-file-property-2")).isEqualTo("the-value-2");
        } finally {
            teardown();
        }
    }

    @Test
    public void system_properties_are_accessible_in_configuration() {
        setup();

        try {
            Assertions.assertThat(holder.application.getConfiguration().getString("systemProperty")).isEqualTo(System.getProperty("java.vendor"));
        } finally {
            teardown();
        }
    }

    @Test
    public void environment_variables_are_accessible_in_configuration() {
        setup();

        try {
            String javaHome = System.getenv().get("JAVA_HOME");
            if (javaHome == null) {
                Assertions.assertThat(holder.application.getConfiguration().getString("environmentVariable")).isEqualTo("${env:JAVA_HOME}");
            } else {
                Assertions.assertThat(holder.application.getConfiguration().getString("environmentVariable")).isEqualTo(javaHome);
            }
        } finally {
            teardown();
        }
    }

    @Test
    public void class_constants_are_accessible_in_configuration() {
        setup();

        try {
            Assertions.assertThat(holder.application.getConfiguration().getString("constantValue")).isEqualTo(CONSTANT_TEST);
        } finally {
            teardown();
        }
    }

    @Test
    public void empty_configuration_values_yield_empty_string() {
        setup();

        try {
            Assertions.assertThat(holder.application.getConfiguration().getString("emptyValue")).isEqualTo("");
        } finally {
            teardown();
        }
    }

    @Test
    public void non_existent_configuration_values_yield_null() {
        setup();

        try {
            Assertions.assertThat(holder.application.getConfiguration().getString("nonExistentValue")).isNull();
        } finally {
            teardown();
        }
    }

    @Test
    public void props_override_is_working_correctly() {
        setup();

        try {
            Assertions.assertThat(holder.application.getConfiguration().getString("overriddenValue")).isEqualTo("I'm overriding");
            Assertions.assertThat(holder.application.getConfiguration().getString("removedValue")).isNull();
            Assertions.assertThat(holder.application.getConfiguration().getString("emptiedValue")).isEmpty();
            Assertions.assertThat(holder.application.getConfiguration().getString("-removedValue")).isNull();
            Assertions.assertThat(holder.application.getConfiguration().getString("-removedInexistentValue")).isNull();
        } finally {
            teardown();
        }
    }

    @Test
    public void values_can_be_appended() {
        setup();

        try {
            Assertions.assertThat(holder.application.getConfiguration().getStringArray("appendedValue")).contains("val1", "val2");
            Assertions.assertThat(holder.application.getConfiguration().getStringArray("automaticallyAppendedValue")).contains("val1", "val2");
        } finally {
            teardown();
        }
    }

    @Test
    public void json_values_can_be_extracted() {
        System.setProperty("org.seedstack.seed.test.json-value", "{ \"key1\": \"value1\", \"key2\": \"value2\" }");

        setup();
        try {
            Assertions.assertThat(holder.application.getConfiguration().getInt("extractedFromJsonValue")).isEqualTo(2);
            Assertions.assertThat(holder.application.getConfiguration().getString("extractedFromJsonValueWithDollar")).isEqualTo("yop_3");
            Assertions.assertThat(holder.application.getConfiguration().getString("extractedFromJsonValueWithConfiguredPath")).isEqualTo("yip_2");
            Assertions.assertThat(holder.application.getConfiguration().getString("extractedFromIndirectJsonValue")).isEqualTo("value2");
        } finally {
            System.clearProperty("org.seedstack.seed.test.json-value");
            teardown();
        }
    }

    @Test
    public void test_class_configuration() {
        setup();

        try {
            org.apache.commons.configuration.Configuration entityConf = holder.application.getConfiguration(MyClass.class);
            Assertions.assertThat(entityConf).isNotNull();
            Assertions.assertThat(entityConf.getString("test")).isEqualTo("*");
            Assertions.assertThat(entityConf.getString("test1")).isEqualTo(
                    "org.seedstack.*");
            Assertions.assertThat(entityConf.getString("test2")).isEqualTo(
                    "org.seedstack.*");
            Assertions.assertThat(entityConf.getString("test3")).isEqualTo(
                    MyClass.class.getName());
        } finally {
            teardown();
        }
    }

    private void setup() {
        underTest = createKernel(newKernelConfiguration());
        underTest.init();
        underTest.start();

        Module aggregationModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Holder.class);
            }
        };

        holder = underTest.objectGraph().as(Injector.class).createChildInjector(aggregationModule).getInstance(Holder.class);
    }

    private void teardown() {
        underTest.stop();
        underTest = null;
        holder = null;
    }
}
