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
import org.seedstack.seed.Application;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.Logging;
import org.seedstack.seed.core.fixtures.SomeEnum;
import org.seedstack.seed.core.internal.sample.MyClass;
import org.slf4j.Logger;

import javax.inject.Inject;

import static io.nuun.kernel.core.NuunCore.createKernel;
import static io.nuun.kernel.core.NuunCore.newKernelConfiguration;

public class ConfigurationIT {
    public static final String CONSTANT_TEST = "this is a test";

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
        Kernel kernel = setup();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder).isNotNull();
            Assertions.assertThat(holder.application).isNotNull();
            Assertions.assertThat(holder.secret1).isNotNull().isEqualTo("**I am Alice**");
            Assertions.assertThat(holder.dummy).isNotNull().isEqualTo("defaultValue");
            Assertions.assertThat(holder.anInt).isNotNull().isEqualTo(5);
            Assertions.assertThat(holder.someShorts).isNotEmpty().isEqualTo(new short[]{2, 3, 4});
            Assertions.assertThat(holder.someEnum).isNotNull().isEqualTo(SomeEnum.FOO);
            Assertions.assertThat(holder.logger).isNotNull();
            Assertions.assertThat(holder.logger.getName()).isEqualTo(Holder.class.getName());
        } finally {
            teardown(kernel);
        }
    }

    @Test
    public void injector_graphing_is_working_correctly() throws Exception {
        Kernel kernel = setup();

        try {
            Holder holder = getHolder(kernel);
            String injectorGraph = holder.application.getInjectionGraph(null);

            Assertions.assertThat(injectorGraph).isNotNull();
            Assertions.assertThat(injectorGraph).isNotEmpty();
        } finally {
            teardown(kernel);
        }
    }

    @Test
    public void configuration_can_be_retrieved() {
        Kernel kernel = setup();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("org.seedstack.seed.core.application-id")).isEqualTo("seed-it");
        } finally {
            teardown(kernel);
        }
    }

    @Test
    public void only_metainf_configuration_is_considered_for_configuration() {
        Kernel kernel = setup();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("root-properties-test")).isNull();
            Assertions.assertThat(holder.application.getConfiguration().getString("root-props-test")).isNull();
        } finally {
            teardown(kernel);
        }
    }

    @Test
    public void advanced_configuration_usage_is_working() {
        Kernel kernel = setup();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("root-property")).isEqualTo("the-root-value");
            Assertions.assertThat(holder.application.getConfiguration().getString("secret1")).isEqualTo("**I am Alice**");
            Assertions.assertThat(holder.application.getConfiguration().getString("secret2")).isEqualTo("**I am Bob**");
        } finally {
            teardown(kernel);
        }
    }

    @Test
    public void properties_and_props_are_loaded() {
        Kernel kernel = setup();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("any-file-property-1")).isEqualTo("the-value-1");
            Assertions.assertThat(holder.application.getConfiguration().getString("any-file-property-2")).isEqualTo("the-value-2");
        } finally {
            teardown(kernel);
        }
    }

    @Test
    public void system_properties_are_accessible_in_configuration() {
        Kernel kernel = setup();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("systemProperty")).isEqualTo(System.getProperty("java.vendor"));
        } finally {
            teardown(kernel);
        }
    }

    @Test
    public void environment_variables_are_accessible_in_configuration() {
        Kernel kernel = setup();

        try {
            Holder holder = getHolder(kernel);
            String javaHome = System.getenv().get("JAVA_HOME");
            if (javaHome == null) {
                Assertions.assertThat(holder.application.getConfiguration().getString("environmentVariable")).isEqualTo("${env:JAVA_HOME}");
            } else {
                Assertions.assertThat(holder.application.getConfiguration().getString("environmentVariable")).isEqualTo(javaHome);
            }
        } finally {
            teardown(kernel);
        }
    }

    @Test
    public void class_constants_are_accessible_in_configuration() {
        Kernel kernel = setup();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("constantValue")).isEqualTo(CONSTANT_TEST);
        } finally {
            teardown(kernel);
        }
    }

    @Test
    public void empty_configuration_values_yield_empty_string() {
        Kernel kernel = setup();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("emptyValue")).isEqualTo("");
        } finally {
            teardown(kernel);
        }
    }

    @Test
    public void non_existent_configuration_values_yield_null() {
        Kernel kernel = setup();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("nonExistentValue")).isNull();
        } finally {
            teardown(kernel);
        }
    }

    @Test
    public void props_override_is_working_correctly() {
        Kernel kernel = setup();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getString("overriddenValue")).isEqualTo("I'm overriding");
            Assertions.assertThat(holder.application.getConfiguration().getString("removedValue")).isNull();
            Assertions.assertThat(holder.application.getConfiguration().getString("emptiedValue")).isEmpty();
            Assertions.assertThat(holder.application.getConfiguration().getString("-removedValue")).isNull();
            Assertions.assertThat(holder.application.getConfiguration().getString("-removedInexistentValue")).isNull();
        } finally {
            teardown(kernel);
        }
    }

    @Test
    public void values_can_be_appended() {
        Kernel kernel = setup();

        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getStringArray("appendedValue")).contains("val1", "val2");
            Assertions.assertThat(holder.application.getConfiguration().getStringArray("automaticallyAppendedValue")).contains("val1", "val2");
        } finally {
            teardown(kernel);
        }
    }

    @Test
    public void json_values_can_be_extracted() {
        System.setProperty("org.seedstack.seed.test.json-value", "{ \"key1\": \"value1\", \"key2\": \"value2\" }");

        Kernel kernel = setup();
        try {
            Holder holder = getHolder(kernel);
            Assertions.assertThat(holder.application.getConfiguration().getInt("extractedFromJsonValue")).isEqualTo(2);
            Assertions.assertThat(holder.application.getConfiguration().getString("extractedFromJsonValueWithDollar")).isEqualTo("yop_3");
            Assertions.assertThat(holder.application.getConfiguration().getString("extractedFromJsonValueWithConfiguredPath")).isEqualTo("yip_2");
            Assertions.assertThat(holder.application.getConfiguration().getString("extractedFromIndirectJsonValue")).isEqualTo("value2");
        } finally {
            System.clearProperty("org.seedstack.seed.test.json-value");
            teardown(kernel);
        }
    }

    @Test
    public void test_class_configuration() {
        Kernel kernel = setup();

        try {
            Holder holder = getHolder(kernel);
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
