/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.jndi;

import io.nuun.kernel.api.plugin.context.InitContext;
import org.assertj.core.api.Assertions;
import org.fest.reflect.core.Reflection;
import org.fest.reflect.reference.TypeRef;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.Application;
import org.seedstack.seed.JndiConfig;
import org.seedstack.seed.spi.config.ApplicationProvider;

import javax.naming.Context;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JndiPluginTest {
    private JndiPlugin pluginUnderTest;

    @Before
    public void setUp() throws Exception {
        pluginUnderTest = new JndiPlugin();
    }

    @Test
    public void initTest() {
        JndiConfig jndiConfig = new JndiConfig();
        jndiConfig.addAdditionalContext("test1", "/jndi-test1.properties");
        Whitebox.setInternalState(pluginUnderTest, "jndiConfig", jndiConfig);

        InitContext initContext = mock(InitContext.class);
        Application application = mock(Application.class);
        when(application.getConfiguration()).thenReturn(Coffig.builder().build());
        when(initContext.dependency(ApplicationProvider.class)).thenReturn(() -> application);
        pluginUnderTest.init(initContext);
        Assertions.assertThat(pluginUnderTest.nativeUnitModule()).isInstanceOf(JndiModule.class);
        Map<String, Context> additionalJndiContexts = Reflection.field("additionalJndiContexts").ofType(new TypeRef<Map<String, Context>>() {
        }).in(pluginUnderTest).get();
        Context defaultJndiContext = Reflection.field("defaultJndiContext").ofType(Context.class).in(pluginUnderTest).get();
        Assertions.assertThat(additionalJndiContexts).isNotNull();
        Assertions.assertThat(additionalJndiContexts.containsKey("test1")).isNotNull();
        Assertions.assertThat(defaultJndiContext).isNotNull();
    }

    @Test
    public void nameTest() {
        Assertions.assertThat(pluginUnderTest.name()).isNotNull();
    }
}
