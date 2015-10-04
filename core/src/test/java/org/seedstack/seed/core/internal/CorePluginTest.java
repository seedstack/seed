/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.api.Install;
import org.seedstack.seed.core.spi.diagnostic.DiagnosticInfoCollector;
import io.nuun.kernel.api.plugin.context.InitContext;
import org.assertj.core.api.Assertions;
import org.fest.reflect.core.Reflection;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * CorePlugin unit test
 *
 * @author redouane.loulou@ext.mpsa.com
 */
public class CorePluginTest {
    public static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
        }
    }

    CorePlugin pluginUnderTest;

    @Before
    public void before() {
        pluginUnderTest = new CorePlugin();
    }

    @Test
    public void verify_nativeUnitModule_instance() {
        Object object = pluginUnderTest.nativeUnitModule();
        Assertions.assertThat(object).isNotNull();
        Assertions.assertThat(object).isInstanceOf(Module.class);
    }

    @Test
    public void initCorePluginTest() {
        InitContext initContext = mockInitContextForCore(TestModule.class, null);
        pluginUnderTest.init(initContext);
        Object object = pluginUnderTest.nativeUnitModule();
        Assertions.assertThat(object).isNotNull();
        Assertions.assertThat(object).isInstanceOf(Module.class);
        Set<Class<? extends Module>> seedModules = Reflection.field("seedModules").ofType(Set.class).in(pluginUnderTest).get();
        Assertions.assertThat(seedModules).hasSize(1);
    }

    @Test
    public void initCorePluginTest2() {
        InitContext initContext = mockInitContextForCore(Object.class, null);
        pluginUnderTest.init(initContext);
        Object object = pluginUnderTest.nativeUnitModule();
        Assertions.assertThat(object).isNotNull();
        Assertions.assertThat(object).isInstanceOf(Module.class);
        Set<Class<? extends Module>> seedModules = Reflection.field("seedModules").ofType(Set.class).in(pluginUnderTest).get();
        Assertions.assertThat(seedModules).hasSize(0);
    }

    @Test
    public void pluginPackageRootTest() {
        Assertions.assertThat(pluginUnderTest.pluginPackageRoot()).isNotNull();
    }

    @Test
    public void pluginPropertiesPrefixTest() {
        Assertions.assertThat(pluginUnderTest.pluginPropertiesPrefix()).isNotNull();
    }

    @Test
    public void classpathScanRequestsTest() {
        Assertions.assertThat(pluginUnderTest.classpathScanRequests()).hasSize(2);
    }

    @Test
    public void nameTest() {
        Assertions.assertThat(pluginUnderTest.name()).isNotNull();
    }

    public InitContext mockInitContextForCore(Class<?> moduleClass, Class<?> diagnosticClass) {
        InitContext initContext = mock(InitContext.class);

        Map<Class<? extends Annotation>, Collection<Class<?>>> scannedClassesByAnnotationClass = new HashMap<Class<? extends Annotation>, Collection<Class<?>>>();
        Collection<Class<?>> classs = new ArrayList<Class<?>>();
        if (moduleClass != null) {
            classs.add(moduleClass);
        }
        scannedClassesByAnnotationClass.put(Install.class, classs);

        Map<Class<?>, Collection<Class<?>>> scannedSubTypesByParentClass = new HashMap<Class<?>, Collection<Class<?>>>();
        Collection<Class<?>> classs2 = new ArrayList<Class<?>>();
        if (diagnosticClass != null) {
            classs.add(diagnosticClass);
        }
        scannedSubTypesByParentClass.put(DiagnosticInfoCollector.class, classs2);

        when(initContext.scannedClassesByAnnotationClass()).thenReturn(scannedClassesByAnnotationClass);
        when(initContext.scannedSubTypesByParentClass()).thenReturn(scannedSubTypesByParentClass);

        return initContext;
    }

    @Test
    public void load_seed_bootstrap_properties() {
        Configuration bootstrapConfiguration = pluginUnderTest.getBootstrapConfiguration();
        Assertions.assertThat(bootstrapConfiguration).isNotNull();
        Assertions.assertThat(bootstrapConfiguration.getString("package-roots")).isNotEmpty();
    }

    @Test
    public void package_root_should_valid() {
        String pluginPackageRoot = pluginUnderTest.pluginPackageRoot();
        Assertions.assertThat(pluginPackageRoot).contains(CorePlugin.SEED_PACKAGE_ROOT);
        Assertions.assertThat(pluginPackageRoot).contains("some.other.pkg");
    }
}
