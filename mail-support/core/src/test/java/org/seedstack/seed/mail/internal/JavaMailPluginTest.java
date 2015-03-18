/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.mail.internal;

import com.google.common.collect.Lists;
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.assertj.core.api.Condition;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.mail.Session;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JavaMailPluginTest {

    private static JavaMailPlugin mailPlugin;
    @Mock
    private Context context;

    @Mock
    private InitContext initContext;

    @Mock
    private Plugin plugin;

    @Mock
    private ApplicationPlugin applicationPlugin;

    @Mock
    private Configuration configuration;

    @Mock
    private Application application;

    @BeforeClass
    public static void setUp() throws Exception {
        mailPlugin = new JavaMailPlugin();
    }

    @Test(expected = PluginException.class)
    public void test_fail_if_configuration_is_absent() {
        mailPlugin.failIfConfigurationIsAbsent(null);
    }

    @Test
    public void test_get_plugin_configuration() {
        configureMocks();
        when(applicationPlugin.getApplication().getConfiguration()).thenReturn(new MapConfiguration(new HashMap<String, Object>() {{
            put("smtp", Session.getInstance(new Properties()));
        }}));
        final Configuration pluginConfiguration = mailPlugin.getPluginConfiguration(applicationPlugin);
        assertThat(pluginConfiguration).isNotNull();
    }

    @Test
    public void test_init() throws Exception {
        mailPlugin.init(initContext());
    }

    @Test
    public void test_Required_Plugins() throws Exception {
        final Collection<Class<? extends Plugin>> plugins = mailPlugin.requiredPlugins();
        assertThat(plugins).isNotNull();
        assertThat(plugins).isNotEmpty();
        assertThat(plugins).hasSize(1);
        assertThat(plugins).has(new Condition<Iterable<? extends Class<? extends Plugin>>>() {
            @Override
            public boolean matches(Iterable<? extends Class<? extends Plugin>> classes) {
                return classes.iterator().next() != null;
            }
        });
    }

    private void configureMocks() {
        when(applicationPlugin.getApplication()).thenReturn(application);
        when(configuration.getList(anyString())).thenReturn(Lists.newArrayList());
        when(configuration.subset(anyString())).thenReturn(configuration);
    }

    @SuppressWarnings("unchecked")
    private InitContext initContext() {
        configureMocks();

        when(applicationPlugin.getApplication().getConfiguration()).thenReturn(configuration);
        when(plugin.requiredPlugins()).thenReturn(new ArrayList<Class<? extends Plugin>>() {{
            add(ApplicationPlugin.class);
        }});

        Collection pluginsRequired = new ArrayList<Plugin>();
        pluginsRequired.add(applicationPlugin);
        when(initContext.pluginsRequired()).thenReturn(pluginsRequired);

        return initContext;
    }
}