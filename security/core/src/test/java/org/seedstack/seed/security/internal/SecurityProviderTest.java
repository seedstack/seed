/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.nuun.kernel.api.plugin.context.InitContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.Application;
import org.seedstack.seed.core.internal.el.ELPlugin;
import org.seedstack.seed.security.Realm;
import org.seedstack.seed.security.SecurityConfig;
import org.seedstack.seed.security.internal.realms.ConfigurationRealm;
import org.seedstack.seed.spi.ApplicationProvider;

public class SecurityProviderTest {
    private SecurityPlugin underTest;

    @Before
    public void before() {
        underTest = new SecurityPlugin();
        underTest.init(buildCoherentInitContext());
    }

    @Test
    public void verify_dependencies() {
        Collection<Class<?>> plugins = underTest.requiredPlugins();
        assertTrue(plugins.contains(ApplicationProvider.class));
    }

    @Test
    public void testConstructor() {

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    InitContext buildCoherentInitContext() {
        InitContext initContext = mock(InitContext.class);
        Application application = mock(Application.class);
        when(application.getConfiguration()).thenReturn(Coffig.builder().build());

        Map<Class<?>, Collection<Class<?>>> types = new HashMap<>();
        Collection<Class<?>> realms = new ArrayList<>();
        realms.add(ConfigurationRealm.class);
        types.put(Realm.class, realms);
        when(initContext.scannedSubTypesByAncestorClass()).thenReturn(types);

        ApplicationProvider applicationProvider = mock(ApplicationProvider.class);
        SecurityConfig securityConfig = mock(SecurityConfig.class);
        when(initContext.dependency(ApplicationProvider.class)).thenReturn(applicationProvider);
        Coffig coffig = mock(Coffig.class);
        when(coffig.get(SecurityConfig.class)).thenReturn(securityConfig);
        when(initContext.dependency(ApplicationProvider.class)).thenReturn(() -> application);
        // Not pretty because we use real ELPlugin to avoid mocking static methods
        when(initContext.dependency(ELPlugin.class)).thenReturn(new ELPlugin());

        return initContext;
    }
}
