/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.dependency;

import com.google.inject.AbstractModule;
import io.nuun.kernel.api.plugin.context.InitContext;
import mockit.Expectations;
import mockit.Mocked;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.Application;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.spi.config.ApplicationProvider;
import org.seedstack.seed.spi.dependency.DependencyProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DependencyPluginTest {
    private DependencyPlugin dependencyPlugin;

    @Before
    public void before() {
        dependencyPlugin = new DependencyPlugin();
    }

    @Test
    public void verify_nativeUnitModule_instance() {
        Object object = dependencyPlugin.nativeUnitModule();
        Assertions.assertThat(object).isInstanceOf(DependencyModule.class);
    }

    @Test
    public void nameTest() {
        Assertions.assertThat(dependencyPlugin.name()).isNotNull();
    }

    private InitContext mockInitContextForCore(Class<?> dependencyClass) {
        InitContext initContext = mock(InitContext.class);
        Application application = mock(Application.class);

        Map<Class<?>, Collection<Class<?>>> scannedSubTypesByParentClass = new HashMap<>();
        Collection<Class<?>> providerClasses = new ArrayList<>();
        if (dependencyClass != null) {
            providerClasses.add(dependencyClass);
        }
        scannedSubTypesByParentClass.put(DependencyProvider.class, providerClasses);

        when(application.getConfiguration()).thenReturn(Coffig.builder().build());
        when(initContext.dependency(ApplicationProvider.class)).thenReturn(() -> application);
        when(initContext.scannedSubTypesByParentClass()).thenReturn(scannedSubTypesByParentClass);

        return initContext;
    }

    @Test
    public void checkOptionalDependency(@Mocked final DependencyProvider myProvider) {
        new Expectations() {
            {
                myProvider.getClassToCheck();
                result = "java.lang.String";
            }
        };
        InitContext initContext = mockInitContextForCore(myProvider.getClass());
        dependencyPlugin.init(initContext);
        Optional<?> optionalDependency = dependencyPlugin.getDependency(myProvider.getClass());
        Assertions.assertThat(optionalDependency).isNotNull();
        Assertions.assertThat(optionalDependency.isPresent()).isTrue();
    }

    @Test
    public void checkOptionalDependencyNOK(@Mocked final DependencyProvider myProvider) {
        new Expectations() {
            {
                myProvider.getClassToCheck();
                result = "xxxxx";
            }
        };
        InitContext initContext = mockInitContextForCore(myProvider.getClass());
        dependencyPlugin.init(initContext);
        Optional<?> optionalDependency = dependencyPlugin.getDependency(myProvider.getClass());
        Assertions.assertThat(optionalDependency).isNotNull();
        Assertions.assertThat(optionalDependency.isPresent()).isFalse();
    }

    @Test(expected = SeedException.class)
    public void checkOptionalDependencyWithInstantiationError() {
        InitContext initContext = mockInitContextForCore(DependencyProvider.class);
        dependencyPlugin.init(initContext);
    }

}
