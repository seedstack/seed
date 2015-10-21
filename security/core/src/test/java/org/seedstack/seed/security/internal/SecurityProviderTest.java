/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import io.nuun.kernel.api.plugin.context.InitContext;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;
import org.seedstack.seed.el.internal.ELPlugin;
import org.seedstack.seed.security.Realm;
import org.seedstack.seed.security.internal.realms.ConfigurationRealm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SecurityProviderTest {

    SecurityPlugin underTest;
    
    @Before
    public void before() {
        underTest = new SecurityPlugin();
        underTest.init(buildCoherentInitContext());
    }

    @Test
    public void verify_dependencies() {
        Collection<Class<?>> plugins = underTest.requiredPlugins();
        assertTrue(plugins.contains(ConfigurationProvider.class));
    }

    @Test
    public void testConstructor() {
        
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    InitContext buildCoherentInitContext() {
        InitContext initContext = mock(InitContext.class);

        Map<Class<?>, Collection<Class<?>>> types = new HashMap<Class<?>, Collection<Class<?>>>();
        Collection<Class<?>> realms = new ArrayList<Class<?>>();
        realms.add(ConfigurationRealm.class);
        types.put(Realm.class, realms);
        when(initContext.scannedSubTypesByAncestorClass()).thenReturn(types);

        ApplicationPlugin appPlugin = mock(ApplicationPlugin.class);
        ELPlugin elPlugin = mock(ELPlugin.class);
        when(elPlugin.isDisabled()).thenReturn(false);
        Configuration conf = mock(Configuration.class);
        when(initContext.dependency(ConfigurationProvider.class)).thenReturn(appPlugin);
        when(appPlugin.getConfiguration()).thenReturn(conf);
        when(initContext.dependency(ELPlugin.class)).thenReturn(elPlugin);

        return initContext;
    }
}
