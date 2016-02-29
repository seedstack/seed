/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.context.InitContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.provider.PrioritizedProvider;
import org.seedstack.seed.core.internal.init.DiagnosticManagerImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurationPluginTest {
    private ConfigurationPlugin configurationPlugin;

    @Before
    public void before() {
        configurationPlugin = new ConfigurationPlugin();
        Coffig coffig = Coffig.builder().withProviders(new PrioritizedProvider()).build();
        Whitebox.setInternalState(configurationPlugin, "configuration", coffig);
        Whitebox.setInternalState(configurationPlugin, "diagnosticManager", new DiagnosticManagerImpl());
    }

    @Test
    public void nameTest() {
        assertThat(configurationPlugin.name()).isNotEmpty();
    }

    @Test
    public void verify_nativeUnitModule_instance() {
        Object object = configurationPlugin.nativeUnitModule();
        assertThat(object).isInstanceOf(ConfigurationModule.class);
    }

    @Test
    public void initPluginTest() {
        InitContext initContext = mockInitContextForCore(Lists.newArrayList("META-INF/configuration/file.yaml", "bad.yaml"), Lists.newArrayList("META-INF/configuration/file.json", "bad.json"));
        configurationPlugin.init(initContext);
    }

    private InitContext mockInitContextForCore(ArrayList<String> yamlFiles, ArrayList<String> jsonFiles) {
        InitContext initContext = mock(InitContext.class);

        Map<String, Collection<String>> mapResourcesByRegex = new HashMap<>();
        mapResourcesByRegex.put(".*\\.yaml", yamlFiles);
        mapResourcesByRegex.put(".*\\.json", jsonFiles);

        when(initContext.mapResourcesByRegex()).thenReturn(mapResourcesByRegex);

        return initContext;
    }

    @Test
    public void package_root_should_valid() {
        String pluginPackageRoot = configurationPlugin.pluginPackageRoot();
        assertThat(pluginPackageRoot).contains("META-INF.configuration");
    }
}
