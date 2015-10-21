/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.transaction.internal;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import org.apache.commons.configuration.Configuration;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Fail;
import org.fest.reflect.core.Reflection;
import org.fest.reflect.reference.TypeRef;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.Application;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.transaction.Transactional;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionHandlerTestImpl;
import org.seedstack.seed.transaction.spi.TransactionManager;
import org.seedstack.seed.transaction.spi.TransactionMetadataResolver;
import org.seedstack.seed.transaction.spi.TransactionMetadataResolverTestImpl;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionPluginTest {

    TransactionPlugin pluginUnderTest;

    private static String TRANSACTION_HANDLER_IMPL_CLASS_NAME = TransactionHandlerTestImpl.class.getName();

    @Before
    public void before() {
        pluginUnderTest = new TransactionPlugin();
    }

    @Test
    public void initTransactionPluginTest1() {
        InitState initState = pluginUnderTest.init(mockInitContext(LocalTransactionManager.class, TRANSACTION_HANDLER_IMPL_CLASS_NAME));
        Assertions.assertThat(initState).isEqualTo(InitState.INITIALIZED);
    }

    @Test
    public void initTransactionPluginTest2() {
        InitState initState = pluginUnderTest.init(mockInitContext(LocalTransactionManager.class, TRANSACTION_HANDLER_IMPL_CLASS_NAME));
        Object object = pluginUnderTest.nativeUnitModule();
        Assertions.assertThat(object).isNotNull();
        Assertions.assertThat(object).isInstanceOf(TransactionModule.class);
        Class<?> defaultTransactionHandlerClass = Reflection.field("defaultTransactionHandlerClass").ofType(Class.class).in(pluginUnderTest).get();
        Assertions.assertThat(defaultTransactionHandlerClass).isNotNull();
        Set<TransactionMetadataResolver> transactionMetadataResolvers = Reflection.field("transactionMetadataResolvers").ofType(new TypeRef<Set<TransactionMetadataResolver>>() {
        }).in(pluginUnderTest).get();
        Assertions.assertThat(transactionMetadataResolvers).isNotNull();
        TransactionManager transactionManager = Reflection.field("transactionManager").ofType(TransactionManager.class).in(pluginUnderTest).get();
        Assertions.assertThat(transactionManager).isNotNull();
        Assertions.assertThat(initState).isEqualTo(InitState.INITIALIZED);
    }

    @Test
    public void initTransactionPluginTest3() {
        Collection<Class<?>> implementsTransactionHandlerClasses = new ArrayList<Class<?>>();
        implementsTransactionHandlerClasses.add(TransactionHandlerTestImpl.class);
        implementsTransactionHandlerClasses.add(TransactionHandlerTestImpl.class);
        InitState initState = pluginUnderTest.init(mockInitContext2(LocalTransactionManager.class, implementsTransactionHandlerClasses));
        Assertions.assertThat(initState).isEqualTo(InitState.INITIALIZED);
        Object object = pluginUnderTest.nativeUnitModule();
        Assertions.assertThat(object).isNotNull();
        Assertions.assertThat(object).isInstanceOf(TransactionModule.class);

    }

    @Test
    public void initTransactionPluginTest4() {
        Collection<Class<?>> implementsTransactionHandlerClasses = new ArrayList<Class<?>>();
        implementsTransactionHandlerClasses.add(TransactionHandlerTestImpl.class);
        InitState initState = pluginUnderTest.init(mockInitContext2(LocalTransactionManager.class, implementsTransactionHandlerClasses));
        Assertions.assertThat(initState).isEqualTo(InitState.INITIALIZED);
        Object object = pluginUnderTest.nativeUnitModule();
        Assertions.assertThat(object).isNotNull();
        Assertions.assertThat(object).isInstanceOf(TransactionModule.class);

    }

    @Test(expected = RuntimeException.class)
    public void initTransactionPluginTest5() {
        pluginUnderTest.init(mockInitContext(Object.class, TRANSACTION_HANDLER_IMPL_CLASS_NAME));
    }

    @Test(expected = PluginException.class)
    public void initTransactionPluginTest6() {
        pluginUnderTest.init(mockInitContext(LocalTransactionManager.class, "Toto"));
    }

    @Test
    public void initTransactionPluginTest7() {
        InitState initState = pluginUnderTest.init(mockInitContext(null, TRANSACTION_HANDLER_IMPL_CLASS_NAME));
        Assertions.assertThat(initState).isEqualTo(InitState.INITIALIZED);
        Class<?> defaultTransactionHandlerClass = Reflection.field("defaultTransactionHandlerClass").ofType(Class.class).in(pluginUnderTest).get();
        Assertions.assertThat(defaultTransactionHandlerClass).isNotNull();
        Set<TransactionMetadataResolver> transactionMetadataResolvers = Reflection.field("transactionMetadataResolvers").ofType(new TypeRef<Set<TransactionMetadataResolver>>() {
        }).in(pluginUnderTest).get();
        Assertions.assertThat(transactionMetadataResolvers).isNotNull();
        TransactionManager transactionManager = Reflection.field("transactionManager").ofType(TransactionManager.class).in(pluginUnderTest).get();
        Assertions.assertThat(transactionManager).isNotNull();
    }

    @Test
    public void isTransactionalTest() {
        try {
            Assertions.assertThat(pluginUnderTest.isTransactional(this.getClass().getDeclaredMethod("testTransactional"))).isTrue();
        } catch (SecurityException e) {
            Fail.fail(e.getMessage());
        } catch (NoSuchMethodException e) {
            Fail.fail(e.getMessage());
        }
    }

    @Test
    public void requiredPluginsTest() {
        Assertions.assertThat(pluginUnderTest.requiredPlugins()).isNotNull();
    }

    @Transactional
    public void testTransactional() {

    }

    @SuppressWarnings("unchecked")
    private InitContext mockInitContext(Class<?> transactionManagerClass, String transactionHandlerClass) {
        InitContext initContext = mock(InitContext.class);
        Configuration configuration = mock(Configuration.class);
        ApplicationPlugin applicationPlugin = mock(ApplicationPlugin.class);
        Application application = mock(Application.class);
        when(applicationPlugin.getApplication()).thenReturn(application);
        when(application.getConfiguration()).thenReturn(configuration);
        when(configuration.subset(TransactionPlugin.TRANSACTION_PLUGIN_CONFIGURATION_PREFIX)).thenReturn(configuration);
        if (transactionManagerClass != null) {
            when(configuration.subset(TransactionPlugin.TRANSACTION_PLUGIN_CONFIGURATION_PREFIX).getString("manager")).thenReturn(
                    transactionManagerClass.getName());
        }
        if (transactionHandlerClass != null) {
            when(configuration.subset(TransactionPlugin.TRANSACTION_PLUGIN_CONFIGURATION_PREFIX).getString("default-handler")).thenReturn(
                    transactionHandlerClass);
        }
        Map<Class<?>, Collection<Class<?>>> mapImplements = new HashMap<Class<?>, Collection<Class<?>>>();
        Collection<Class<?>> implementsTransactionHandlerClasses = new ArrayList<Class<?>>();
        implementsTransactionHandlerClasses.add(TransactionHandlerTestImpl.class);
        mapImplements.put(TransactionHandler.class, implementsTransactionHandlerClasses);
        Collection<Class<?>> implementsTransactionMetadataResolverClasses = new ArrayList<Class<?>>();
        implementsTransactionMetadataResolverClasses.add(TransactionMetadataResolverTestImpl.class);
        mapImplements.put(TransactionMetadataResolver.class, implementsTransactionMetadataResolverClasses);
        when(initContext.scannedSubTypesByParentClass()).thenReturn(mapImplements);
        when(initContext.dependency(ConfigurationProvider.class)).thenReturn(applicationPlugin);
        when(applicationPlugin.getConfiguration()).thenReturn(configuration);
        return initContext;
    }

    public InitContext mockInitContext2(Class<?> transactionManagerClass, Collection<Class<?>> implementsTransactionHandler) {
        InitContext initContext = mockInitContext(transactionManagerClass, null);
        Map<Class<?>, Collection<Class<?>>> mapImplementsTransactionHandler = new HashMap<Class<?>, Collection<Class<?>>>();
        mapImplementsTransactionHandler.put(TransactionHandler.class, implementsTransactionHandler);
        when(initContext.scannedSubTypesByParentClass()).thenReturn(mapImplementsTransactionHandler);
        return initContext;
    }

}
