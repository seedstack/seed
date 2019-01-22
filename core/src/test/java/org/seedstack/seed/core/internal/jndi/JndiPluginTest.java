/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.jndi;

import io.nuun.kernel.api.plugin.context.InitContext;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Tested;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.JndiConfig;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;

public class JndiPluginTest {
    @Tested
    private JndiPlugin pluginUnderTest;

    @Test
    public void initTest(@Mocked InitContext initContext) {
        new MockUp<AbstractSeedPlugin>() {
            @Mock
            public JndiConfig getConfiguration(Class configClass, String... path) {
                return new JndiConfig().addAdditionalContext("test1", "jndi-test1.properties");
            }
        };
        pluginUnderTest.initialize(initContext);
        Assertions.assertThat(pluginUnderTest.nativeUnitModule()).isInstanceOf(JndiModule.class);
        Assertions.assertThat(pluginUnderTest.getJndiContexts().get("default")).isNotNull();
        Assertions.assertThat(pluginUnderTest.getJndiContexts().get("test1")).isNotNull();
    }

    @Test
    public void nameTest() {
        Assertions.assertThat(pluginUnderTest.name()).isNotNull();
    }
}
