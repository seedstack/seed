/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.seedstack.seed.Application;
import org.seedstack.seed.core.rules.SeedITRule;
import org.seedstack.seed.core.rules.SystemProperties;

@NotThreadSafe
public class ConfigurationProfilesIT {
    @Rule
    public SeedITRule rule = new SeedITRule(this);
    @Inject
    private Application application;

    @Test
    public void noProfile() {
        Assertions.assertThat(getValue("testProperty")).isEqualTo("baseValue");
        Assertions.assertThat(getValue("debugMode")).isEqualTo("false");
    }

    @Test
    @SystemProperties({"seedstack.profiles", "dev"})
    public void devProfile() {
        Assertions.assertThat(getValue("testProperty")).isEqualTo("devValue");
    }

    @Test
    @SystemProperties({"seedstack.profiles", "preprod"})
    public void preprodProfile() {
        Assertions.assertThat(getValue("testProperty")).isEqualTo("preprodValue");
    }

    @Test
    @SystemProperties({"seedstack.profiles", "prod"})
    public void prodProfile() {
        Assertions.assertThat(getValue("testProperty.subProperty1")).isEqualTo("prodValue");
        Assertions.assertThat(getValue("testProperty.subProperty2")).isEqualTo("prodValue");
    }

    @Test
    @SystemProperties({"seedstack.profiles", "dev, debug"})
    public void devDebugProfiles() {
        Assertions.assertThat(getValue("testProperty")).isEqualTo("devValue");
        Assertions.assertThat(getValue("debugMode")).isEqualTo("true");
    }

    private String getValue(String key) {
        return application.getConfiguration().get(String.class, key);
    }
}