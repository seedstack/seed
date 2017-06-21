/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import io.nuun.kernel.api.plugin.context.InitContext;
import org.assertj.core.api.Assertions;
import org.fest.reflect.core.Reflection;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.Application;
import org.seedstack.seed.Ignore;
import org.seedstack.seed.Install;
import org.seedstack.seed.spi.ApplicationProvider;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CorePluginTest {
    @Install
    @Ignore
    private static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
        }
    }

    @Install(override = true)
    @Ignore
    private static class TestOverridingModule extends AbstractModule {
        @Override
        protected void configure() {
        }
    }

    private CorePlugin corePlugin;

    @Before
    public void before() {
        corePlugin = new CorePlugin();
    }

    @Test
    public void package_root_should_valid() {
        String pluginPackageRoot = corePlugin.pluginPackageRoot();
        Assertions.assertThat(pluginPackageRoot).contains("org.seedstack");
    }

    @Test
    public void verify_nativeUnitModule_instance() {
        Object object = corePlugin.nativeUnitModule();
        Assertions.assertThat(object).isInstanceOf(CoreModule.class);
    }

    @Test
    public void installModule() {
        InitContext initContext = mockInitContextForCore(TestModule.class);
        corePlugin.init(initContext);
        Object object = corePlugin.nativeUnitModule();
        Assertions.assertThat(object).isInstanceOf(CoreModule.class);
        @SuppressWarnings("unchecked")
        Set<Class<? extends Module>> seedModules = Reflection.field("seedModules").ofType(Set.class).in(corePlugin).get();
        Assertions.assertThat(seedModules).hasSize(1);
    }

    @Test
    public void installOverridingModule() {
        InitContext initContext = mockInitContextForCore(TestOverridingModule.class);
        corePlugin.init(initContext);
        Object object = corePlugin.nativeUnitModule();
        Assertions.assertThat(object).isInstanceOf(CoreModule.class);
        @SuppressWarnings("unchecked")
        Set<Class<? extends Module>> seedModules = Reflection.field("seedOverridingModules").ofType(Set.class).in(corePlugin).get();
        Assertions.assertThat(seedModules).hasSize(1);
    }

    @Test
    public void initCorePluginTest2() {
        InitContext initContext = mockInitContextForCore(null);
        corePlugin.init(initContext);
        Object object = corePlugin.nativeUnitModule();
        Assertions.assertThat(object).isInstanceOf(CoreModule.class);
        @SuppressWarnings("unchecked")
        Set<Class<? extends Module>> seedModules = Reflection.field("seedModules").ofType(Set.class).in(corePlugin).get();
        Assertions.assertThat(seedModules).hasSize(0);
    }

    @Test
    public void nameTest() {
        Assertions.assertThat(corePlugin.name()).isNotNull();
    }

    private InitContext mockInitContextForCore(Class<?> moduleClass) {
        InitContext initContext = mock(InitContext.class);
        Application application = mock(Application.class);

        Map<Class<? extends Annotation>, Collection<Class<?>>> scannedClassesByAnnotationClass = new HashMap<>();
        Collection<Class<?>> classs = new ArrayList<>();
        if (moduleClass != null) {
            classs.add(moduleClass);
        }
        scannedClassesByAnnotationClass.put(Install.class, classs);

        when(application.getConfiguration()).thenReturn(Coffig.builder().build());
        when(initContext.dependency(ApplicationProvider.class)).thenReturn(() -> application);
        when(initContext.scannedClassesByAnnotationClass()).thenReturn(scannedClassesByAnnotationClass);

        return initContext;
    }
}
