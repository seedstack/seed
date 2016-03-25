/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;


import org.apache.commons.configuration.Configuration;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;

public class SeedConfigLoaderTest {

    @Test
    public void testBootstrapConfig() {
        Configuration configuration = new SeedConfigLoader().buildBootstrapConfig();

        Assertions.assertThat(configuration).isNotNull();
        Assertions.assertThat(configuration.getString(ApplicationPlugin.BASE_PACKAGES_KEY)).isEqualTo("some.other.pkg");
        Assertions.assertThat(configuration.getString("test.key2")).isEqualTo("val2");
    }

    @Test
    public void environment_variables_are_accessible_in_bootstrap_configuration() {
        Configuration configuration = new SeedConfigLoader().buildBootstrapConfig();

        String javaHome = System.getenv().get("JAVA_HOME");
        if (javaHome == null) {
            Assertions.assertThat(configuration.getString("test.environmentVariable")).isEqualTo("${env:JAVA_HOME}");
        } else {
            Assertions.assertThat(configuration.getString("test.environmentVariable")).isEqualTo(javaHome);
        }
    }

}
