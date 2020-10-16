/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.junit4;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Application;
import org.seedstack.seed.testing.Arguments;
import org.seedstack.seed.testing.ConfigurationProfiles;
import org.seedstack.seed.testing.ConfigurationProperty;
import org.seedstack.seed.testing.KernelParameter;
import org.seedstack.seed.testing.LaunchWith;
import org.seedstack.seed.testing.SystemProperty;
import org.seedstack.seed.testing.junit4.fixtures.TestITLauncher;

@RunWith(SeedITRunner.class)
@ConfigurationProperty(name = "someTestKey", value = "testValue")
@ConfigurationProperty(name = "someTestKey2", value = "testValue2")
@ConfigurationProfiles({"profile1", "profile2"})
@KernelParameter(name = "seedstack.config.someKernelParam", value = "testValue")
@KernelParameter(name = "seedstack.config.someKernelParam2", value = "testValue2")
@Arguments({"testArg", "-o", "testOption"})
@SystemProperty(name = "seedstack.someTestProperty", value = "testValue")
@SystemProperty(name = "seedstack.someTestProperty2", value = "testValue2")
@LaunchWith(TestITLauncher.class)
public class PerClassIT {
    @Inject
    private Application application;

    @Test
    public void injection() {
        assertThat(application).isNotNull();
    }

    @Test
    public void configuration() {
        assertThat(application.getConfiguration().getMandatory(String.class, "someTestKey"))
                .isEqualTo("testValue");
        assertThat(application.getConfiguration().getMandatory(String.class, "someTestKey2"))
                .isEqualTo("testValue2");
    }

    @Test
    public void kernelParameter() {
        assertThat(application.getConfiguration().getMandatory(String.class, "someKernelParam"))
                .isEqualTo("testValue");
        assertThat(application.getConfiguration().getMandatory(String.class, "someKernelParam2"))
                .isEqualTo("testValue2");
    }

    @Test
    public void arguments() {
        assertThat(application.getArguments()).containsExactly("testArg", "-o", "testOption");
    }

    @Test
    public void systemProperty() {
        assertThat(System.getProperty("seedstack.someTestProperty")).isEqualTo("testValue");
        assertThat(System.getProperty("seedstack.someTestProperty2")).isEqualTo("testValue2");
    }

    @Test
    public void configurationProfiles() {
        assertThat(System.getProperty("seedstack.profiles")).isEqualTo("profile1,profile2");
    }
}
