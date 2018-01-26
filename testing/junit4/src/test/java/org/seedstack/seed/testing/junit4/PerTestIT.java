/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.testing.junit4;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Application;
import org.seedstack.seed.testing.Arguments;
import org.seedstack.seed.testing.ConfigurationProfiles;
import org.seedstack.seed.testing.ConfigurationProperty;
import org.seedstack.seed.testing.KernelParameter;
import org.seedstack.seed.testing.LaunchMode;
import org.seedstack.seed.testing.LaunchWith;
import org.seedstack.seed.testing.SystemProperty;
import org.seedstack.seed.testing.junit4.fixtures.TestITLauncher;

@RunWith(SeedITRunner.class)
@LaunchWith(value = TestITLauncher.class, mode = LaunchMode.PER_TEST)
@ConfigurationProperty(name = "someTestKey", value = "testValue")
@ConfigurationProfiles({"profile1", "profile2"})
@KernelParameter(name = "seedstack.config.someKernelParam", value = "testValue")
@Arguments({"testArg", "-o", "testOption"})
@SystemProperty(name = "seedstack.someTestProperty", value = "testValue")
public class PerTestIT {
    @Inject
    private Application application;

    @BeforeClass
    public static void beforeClass() {
        assertThat(System.getProperty("seedstack.someTestProperty")).isNull();
    }

    @AfterClass
    public static void afterClass() {
        assertThat(System.getProperty("seedstack.someTestProperty")).isNull();
    }

    @Test
    public void injection() {
        assertThat(application).isNotNull();
    }

    @Test
    public void configuration() {
        assertThat(application.getConfiguration().getMandatory(String.class, "someTestKey"))
                .isEqualTo("testValue");
    }

    @Test
    @ConfigurationProperty(name = "someOtherTestKey", value = "testValue2")
    public void appendConfiguration() {
        assertThat(application.getConfiguration().getMandatory(String.class, "someTestKey"))
                .isEqualTo("testValue");
        assertThat(application.getConfiguration().getMandatory(String.class, "someOtherTestKey"))
                .isEqualTo("testValue2");
    }

    @Test
    @ConfigurationProperty(name = "someTestKey", value = "testValue2")
    public void overrideConfiguration() {
        assertThat(application.getConfiguration().getMandatory(String.class, "someTestKey"))
                .isEqualTo("testValue2");
    }

    @Test
    public void kernelParameter() {
        assertThat(application.getConfiguration().getMandatory(String.class, "someKernelParam"))
                .isEqualTo("testValue");
    }

    @Test
    @KernelParameter(name = "seedstack.config.someOtherKernelParam", value = "testValue2")
    public void appendKernelParameter() {
        assertThat(application.getConfiguration().getMandatory(String.class, "someKernelParam"))
                .isEqualTo("testValue");
        assertThat(application.getConfiguration().getMandatory(String.class, "someOtherKernelParam"))
                .isEqualTo("testValue2");
    }

    @Test
    @KernelParameter(name = "seedstack.config.someKernelParam", value = "testValue2")
    public void overrideKernelParameter() {
        assertThat(application.getConfiguration().getMandatory(String.class, "someKernelParam"))
                .isEqualTo("testValue2");
    }

    @Test
    public void arguments() {
        assertThat(application.getArguments()).containsExactly("testArg", "-o", "testOption");
    }

    @Test
    @Arguments({"-o2", "testOption2"})
    public void appendArguments() {
        assertThat(application.getArguments()).containsExactly("testArg", "-o", "testOption", "-o2", "testOption2");
    }

    @Test
    @Arguments(value = {"replacementArg", "-o2", "testOption2"}, append = false)
    public void overrideArguments() {
        assertThat(application.getArguments()).containsExactly("replacementArg", "-o2", "testOption2");
    }

    @Test
    public void systemProperty() {
        assertThat(System.getProperty("seedstack.someTestProperty")).isEqualTo("testValue");
    }

    @Test
    @SystemProperty(name = "seedstack.someOtherTestProperty", value = "testValue")
    public void appendSystemProperty() {
        assertThat(System.getProperty("seedstack.someTestProperty")).isEqualTo("testValue");
        assertThat(System.getProperty("seedstack.someOtherTestProperty")).isEqualTo("testValue");
    }

    @Test
    @SystemProperty(name = "seedstack.someTestProperty", value = "testValue2")
    public void overrideSystemProperty() {
        assertThat(System.getProperty("seedstack.someTestProperty")).isEqualTo("testValue2");
    }

    @Test
    public void configurationProfiles() {
        assertThat(System.getProperty("seedstack.profiles")).isEqualTo("profile1,profile2");
    }

    @Test
    @ConfigurationProfiles("profile3")
    public void appendConfigurationProfiles() {
        assertThat(System.getProperty("seedstack.profiles")).isEqualTo("profile1,profile2,profile3");
    }

    @Test
    @ConfigurationProfiles(value = "profile3", append = false)
    public void overrideConfigurationProfiles() {
        assertThat(System.getProperty("seedstack.profiles")).isEqualTo("profile3");
    }
}
