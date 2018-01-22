/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core;

import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Application;
import org.seedstack.seed.testing.LaunchMode;
import org.seedstack.seed.testing.LaunchWith;
import org.seedstack.seed.testing.SystemProperty;
import org.seedstack.seed.testing.junit4.SeedITRunner;

@RunWith(SeedITRunner.class)
@LaunchWith(mode = LaunchMode.PER_TEST)
public class ConfigurationProfilesIT {
    @Inject
    private Application application;

    @Test
    public void noProfile() {
        Assertions.assertThat(getValue("testProperty")).isEqualTo("baseValue");
        Assertions.assertThat(getValue("debugMode")).isEqualTo("false");
    }

    @Test
    @SystemProperty(name = "seedstack.profiles", value = "dev")
    public void devProfile() {
        Assertions.assertThat(getValue("testProperty")).isEqualTo("devValue");
    }

    @Test
    @SystemProperty(name = "seedstack.profiles", value = "preprod")
    public void preprodProfile() {
        Assertions.assertThat(getValue("testProperty")).isEqualTo("preprodValue");
    }

    @Test
    @SystemProperty(name = "seedstack.profiles", value = "prod")
    public void prodProfile() {
        Assertions.assertThat(getValue("testProperty.subProperty1")).isEqualTo("prodValue");
        Assertions.assertThat(getValue("testProperty.subProperty2")).isEqualTo("prodValue");
    }

    @Test
    @SystemProperty(name = "seedstack.profiles", value = "dev, debug")
    public void devDebugProfiles() {
        Assertions.assertThat(getValue("testProperty")).isEqualTo("devValue");
        Assertions.assertThat(getValue("debugMode")).isEqualTo("true");
    }

    private String getValue(String key) {
        return application.getConfiguration().get(String.class, key);
    }
}