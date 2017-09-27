/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.diagnostic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.context.InitContext;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.fest.reflect.core.Reflection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.Application;
import org.seedstack.seed.diagnostic.spi.DiagnosticDomain;
import org.seedstack.seed.diagnostic.spi.DiagnosticInfoCollector;
import org.seedstack.seed.spi.ApplicationProvider;

/**
 * CorePlugin unit test
 */
public class DiagnosticPluginTest {
    private DiagnosticPlugin diagnosticPlugin;

    @Before
    public void before() {
        diagnosticPlugin = new DiagnosticPlugin();
        Whitebox.setInternalState(diagnosticPlugin, "diagnosticManager", new DiagnosticManagerImpl());
    }

    @Test
    public void nameTest() {
        assertThat(diagnosticPlugin.name()).isNotNull();
    }

    @Test
    public void verify_nativeUnitModule_instance() {
        Object object = diagnosticPlugin.nativeUnitModule();
        assertThat(object).isInstanceOf(DiagnosticModule.class);
    }

    @Test
    public void initPluginTest() {
        InitContext initContext = mockInitContextForCore(Lists.newArrayList(TestDiagnosticInfoCollector.class));
        diagnosticPlugin.init(initContext);
        @SuppressWarnings("unchecked")
        Map<String, Class<? extends DiagnosticInfoCollector>> seedModules = Reflection.field(
                "diagnosticInfoCollectorClasses").ofType(Map.class).in(diagnosticPlugin).get();
        assertThat(seedModules).containsExactly(
                new AbstractMap.SimpleImmutableEntry<String, Class<? extends DiagnosticInfoCollector>>("test",
                        TestDiagnosticInfoCollector.class));
    }

    private InitContext mockInitContextForCore(Collection<Class<?>> diagnosticClasses) {
        InitContext initContext = mock(InitContext.class);
        Application application = mock(Application.class);

        Map<Class<?>, Collection<Class<?>>> scannedSubTypesByParentClass = new HashMap<>();
        scannedSubTypesByParentClass.put(DiagnosticInfoCollector.class, diagnosticClasses);

        when(application.getConfiguration()).thenReturn(Coffig.builder().build());
        when(initContext.dependency(ApplicationProvider.class)).thenReturn(() -> application);
        when(initContext.scannedSubTypesByParentClass()).thenReturn(scannedSubTypesByParentClass);

        return initContext;
    }

    @DiagnosticDomain("test")
    private static class TestDiagnosticInfoCollector implements DiagnosticInfoCollector {
        @Override
        public Map<String, Object> collect() {
            return null;
        }
    }
}
