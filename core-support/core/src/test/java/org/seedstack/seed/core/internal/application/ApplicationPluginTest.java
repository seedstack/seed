/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */
package org.seedstack.seed.core.internal.application;

import io.nuun.kernel.api.plugin.context.InitContext;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.internal.CorePlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ApplicationPlugin Test
 *
 * @author redouane.loulou@ext.mpsa.com
 */
public class ApplicationPluginTest {

    ApplicationPlugin pluginUnderTest;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        pluginUnderTest = new ApplicationPlugin();
    }


    @Test
    public void initTest() {
        Collection<String> propertiesFiles = new ArrayList<String>();
        propertiesFiles.add("META-INF/configuration/org.seedstack.seed-test.props");
        Map<String, Collection<String>> mapFile = new HashMap<String, Collection<String>>();
        mapFile.put(".*\\.props", propertiesFiles);
        InitContext initContext = mockInitContextForCore();
        pluginUnderTest.init(initContext);
        Application application = pluginUnderTest.getApplication();
        Assertions.assertThat(application.getConfiguration()).isNotNull();
        Assertions.assertThat(pluginUnderTest.nativeUnitModule()).isInstanceOf(ApplicationModule.class);
    }

    @Test
    public void initTest2() {
        InitContext initContext = mockInitContextForCore();
        pluginUnderTest.init(initContext);
        Application application = pluginUnderTest.getApplication();
        Assertions.assertThat(application).isNotNull();
        Assertions.assertThat(pluginUnderTest.nativeUnitModule()).isInstanceOf(ApplicationModule.class);

    }

    @SuppressWarnings("unchecked")
    public InitContext mockInitContextForCore() {
        InitContext initContext = mock(InitContext.class);
        Map<String, Collection<String>> resources = new HashMap<String, Collection<String>>();

        when(initContext.pluginsRequired()).thenReturn(new ArrayList() {{
            add(mock(CorePlugin.class));
        }});

        Collection<String> props = new ArrayList<String>();
        props.add("META-INF/configuration/org.seedstack.seed-test.props");
        resources.put(ApplicationPlugin.PROPS_REGEX, props);

        Collection<String> properties = new ArrayList<String>();
        properties.add("META-INF/configuration/any.properties");
        resources.put(ApplicationPlugin.PROPERTIES_REGEX, properties);

        when(initContext.mapResourcesByRegex()).thenReturn(resources);
        return initContext;
    }
}
