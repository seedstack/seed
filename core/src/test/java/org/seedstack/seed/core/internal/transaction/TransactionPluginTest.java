/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.transaction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Fail;
import org.fest.reflect.core.Reflection;
import org.fest.reflect.reference.TypeRef;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.Application;
import org.seedstack.seed.core.fixtures.transaction.TransactionHandlerTestImpl;
import org.seedstack.seed.core.fixtures.transaction.TransactionMetadataResolverTestImpl;
import org.seedstack.seed.spi.ApplicationProvider;
import org.seedstack.seed.transaction.TransactionConfig;
import org.seedstack.seed.transaction.Transactional;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionManager;
import org.seedstack.seed.transaction.spi.TransactionMetadataResolver;

public class TransactionPluginTest {
    private TransactionPlugin pluginUnderTest;

    @Before
    public void before() {
        pluginUnderTest = new TransactionPlugin();
    }

    @Test
    public void initTransactionPluginTest1() {
        InitState initState = pluginUnderTest.init(
                mockInitContext(LocalTransactionManager.class, TransactionHandlerTestImpl.class));
        Assertions.assertThat(initState).isEqualTo(InitState.INITIALIZED);
    }

    @Test
    public void initTransactionPluginTest2() {
        InitState initState = pluginUnderTest.init(
                mockInitContext(LocalTransactionManager.class, TransactionHandlerTestImpl.class));
        Object object = pluginUnderTest.nativeUnitModule();
        Assertions.assertThat(object).isNotNull();
        Assertions.assertThat(object).isInstanceOf(TransactionModule.class);
        Set<TransactionMetadataResolver> transactionMetadataResolvers = Reflection.field(
                "transactionMetadataResolvers").ofType(new TypeRef<Set<TransactionMetadataResolver>>() {
        }).in(pluginUnderTest).get();
        Assertions.assertThat(transactionMetadataResolvers).isNotNull();
        TransactionManager transactionManager = Reflection.field("transactionManager").ofType(
                TransactionManager.class).in(pluginUnderTest).get();
        Assertions.assertThat(transactionManager).isNotNull();
        Assertions.assertThat(initState).isEqualTo(InitState.INITIALIZED);
    }

    @Test
    public void initTransactionPluginTest3() {
        Collection<Class<?>> implementsTransactionHandlerClasses = new ArrayList<>();
        implementsTransactionHandlerClasses.add(TransactionHandlerTestImpl.class);
        implementsTransactionHandlerClasses.add(TransactionHandlerTestImpl.class);
        InitState initState = pluginUnderTest.init(
                mockInitContext2(LocalTransactionManager.class, implementsTransactionHandlerClasses));
        Assertions.assertThat(initState).isEqualTo(InitState.INITIALIZED);
        Object object = pluginUnderTest.nativeUnitModule();
        Assertions.assertThat(object).isNotNull();
        Assertions.assertThat(object).isInstanceOf(TransactionModule.class);

    }

    @Test
    public void initTransactionPluginTest4() {
        Collection<Class<?>> implementsTransactionHandlerClasses = new ArrayList<>();
        implementsTransactionHandlerClasses.add(TransactionHandlerTestImpl.class);
        InitState initState = pluginUnderTest.init(
                mockInitContext2(LocalTransactionManager.class, implementsTransactionHandlerClasses));
        Assertions.assertThat(initState).isEqualTo(InitState.INITIALIZED);
        Object object = pluginUnderTest.nativeUnitModule();
        Assertions.assertThat(object).isNotNull();
        Assertions.assertThat(object).isInstanceOf(TransactionModule.class);

    }

    @Test
    public void initTransactionPluginTest7() {
        InitState initState = pluginUnderTest.init(mockInitContext(null, TransactionHandlerTestImpl.class));
        Assertions.assertThat(initState).isEqualTo(InitState.INITIALIZED);
        Set<TransactionMetadataResolver> transactionMetadataResolvers = Reflection.field(
                "transactionMetadataResolvers").ofType(new TypeRef<Set<TransactionMetadataResolver>>() {
        }).in(pluginUnderTest).get();
        Assertions.assertThat(transactionMetadataResolvers).isNotNull();
        TransactionManager transactionManager = Reflection.field("transactionManager").ofType(
                TransactionManager.class).in(pluginUnderTest).get();
        Assertions.assertThat(transactionManager).isNotNull();
    }

    @Test
    public void isTransactionalTest() {
        try {
            Assertions.assertThat(
                    pluginUnderTest.isTransactional(this.getClass().getDeclaredMethod("testTransactional"))).isTrue();
        } catch (SecurityException | NoSuchMethodException e) {
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
    private InitContext mockInitContext(Class<? extends TransactionManager> transactionManagerClass,
            Class<? extends TransactionHandler<?>> transactionHandlerClass) {
        InitContext initContext = mock(InitContext.class);
        Application application = mock(Application.class);

        Coffig coffig = mock(Coffig.class);
        when(coffig.get(TransactionConfig.class)).thenReturn(
                new TransactionConfig().setManager(transactionManagerClass).setDefaultHandler(transactionHandlerClass));
        when(application.getConfiguration()).thenReturn(coffig);
        when(initContext.dependency(ApplicationProvider.class)).thenReturn(() -> application);

        Map<Class<?>, Collection<Class<?>>> mapImplements = new HashMap<>();
        Collection<Class<?>> implementsTransactionHandlerClasses = new ArrayList<>();
        implementsTransactionHandlerClasses.add(TransactionHandlerTestImpl.class);
        mapImplements.put(TransactionHandler.class, implementsTransactionHandlerClasses);
        Collection<Class<?>> implementsTransactionMetadataResolverClasses = new ArrayList<>();
        implementsTransactionMetadataResolverClasses.add(TransactionMetadataResolverTestImpl.class);
        mapImplements.put(TransactionMetadataResolver.class, implementsTransactionMetadataResolverClasses);
        when(initContext.scannedSubTypesByParentClass()).thenReturn(mapImplements);
        return initContext;
    }

    private InitContext mockInitContext2(Class<? extends TransactionManager> transactionManagerClass,
            Collection<Class<?>> implementsTransactionHandler) {
        InitContext initContext = mockInitContext(transactionManagerClass, null);
        Map<Class<?>, Collection<Class<?>>> mapImplements = new HashMap<>();
        mapImplements.put(TransactionHandler.class, implementsTransactionHandler);
        mapImplements.put(TransactionMetadataResolver.class, Collections.emptyList());
        when(initContext.scannedSubTypesByParentClass()).thenReturn(mapImplements);
        return initContext;
    }
}
