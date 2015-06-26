/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.jpa.internal;

import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import org.apache.commons.configuration.Configuration;
import org.assertj.core.api.Assertions;
import org.fest.reflect.core.Reflection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.persistence.jdbc.internal.JdbcPlugin;
import org.seedstack.seed.persistence.jpa.api.JpaExceptionHandler;
import org.seedstack.seed.transaction.internal.TransactionPlugin;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Persistence.class)
public class JpaPluginTest {

    JpaPlugin pluginUnderTest;

    @Before
    public void before() {
        pluginUnderTest = new JpaPlugin();
    }

    @Test
    public void initSeedJpaPluginTest() {
        mockPersitanceCreateEntityManagerFactory();
        pluginUnderTest.init(mockInitContext(
                        mockApplicationPlugin(mockConfiguration("org.seedstack.seed.persistence.jpa.sample.Unit3ExceptionHandler")),
                        mockTransactionPlugin(),
                        mockJdbcPlugin()
                )
        );
        Map<String, JpaExceptionHandler> exceptionHandlerClasses = Reflection.field("exceptionHandlerClasses").ofType(Map.class).in(pluginUnderTest).get();
        Assertions.assertThat(exceptionHandlerClasses).isNotNull();
        Assertions.assertThat(exceptionHandlerClasses).hasSize(1);
    }

    @Test(expected = PluginException.class)
    public void initSeedJpaPluginTest2() {
        pluginUnderTest.init(mockInitContext(
                        mockApplicationPlugin(mockConfiguration("org.seedstack.seed.persistence.jpa.sample.Unit3ExceptionHandler")),
                        mockJdbcPlugin()
                )
        );
        Map<String, JpaExceptionHandler> exceptionHandlerClasses = Reflection.field("exceptionHandlerClasses").ofType(Map.class).in(pluginUnderTest).get();
        Assertions.assertThat(exceptionHandlerClasses).isNotNull();
        Assertions.assertThat(exceptionHandlerClasses).hasSize(1);
    }

    @Test(expected = PluginException.class)
    public void initSeedJpaPluginTest3() {
        pluginUnderTest.init(mockInitContext(mockTransactionPlugin(), mockJdbcPlugin()));
    }

    @Test(expected = PluginException.class)
    public void initSeedJpaPluginTest4() {
        pluginUnderTest.init(mockInitContext(
                        mockApplicationPlugin(mockConfiguration("org.seedstack.seed.persistence.jpa.sample.Unit3ExceptionHandler")),
                        mockTransactionPlugin()
                )
        );
    }

    @Test(expected = PluginException.class)
    public void initSeedJpaPluginTest5() {
        mockPersitanceCreateEntityManagerFactory();
        pluginUnderTest.init(mockInitContext(mockApplicationPlugin(mockConfiguration("toto")), mockTransactionPlugin()));
    }

    public void mockPersitanceCreateEntityManagerFactory() {
        mockStatic(Persistence.class);
        when(Persistence.createEntityManagerFactory("hsql-in-memory", getProperties())).thenReturn(mock(EntityManagerFactory.class));
    }

    private <T extends Plugin> InitContext mockInitContext(T... plugins) {
        InitContext initContext = mock(InitContext.class);
        when(initContext.pluginsRequired()).thenReturn((Collection) Arrays.asList(plugins));
        Assertions.assertThat(initContext).isNotNull();
        return initContext;
    }

    public JdbcPlugin mockJdbcPlugin() {
        return mock(JdbcPlugin.class);
    }

    public ApplicationPlugin mockApplicationPlugin(Configuration configuration) {
        ApplicationPlugin applicationPlugin = mock(ApplicationPlugin.class);
        Application application = mock(Application.class);
        when(applicationPlugin.getApplication()).thenReturn(application);
        when(application.getConfiguration()).thenReturn(configuration);
        return applicationPlugin;
    }

    public Configuration mockConfiguration(String itemExceptionHandlerName) {
        Configuration configuration = mock(Configuration.class);
        Assertions.assertThat(configuration).isNotNull();
        when(configuration.subset(JpaPlugin.JPA_PLUGIN_CONFIGURATION_PREFIX)).thenReturn(configuration);
        when(configuration.getStringArray("units")).thenReturn(new String[]{"hsql-in-memory"});
        when(configuration.subset("unit.hsql-in-memory")).thenReturn(configuration);
        Map<String, String> properties = getProperties();
        when(configuration.getKeys("property")).thenReturn(properties.keySet().iterator());
        for (Entry<String, String> entry : properties.entrySet()) {
            when(configuration.getString(entry.getKey())).thenReturn(entry.getValue());
        }
        when(configuration.getString("exception-handler")).thenReturn(itemExceptionHandlerName);
        return configuration;
    }

    public TransactionPlugin mockTransactionPlugin() {
        return mock(TransactionPlugin.class);
    }

    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("javax.persistence.jdbc.driver", "org.hsqldb.jdbcDriver");
        properties.put("javax.persistence.jdbc.url", "jdbc:hsqldb:mem:testdb");
        properties.put("javax.persistence.jdbc.user", "sa");
        properties.put("javax.persistence.jdbc.password", "");
        properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "create");
        properties.put("sql.enforce_strict_size", "true");
        return properties;
    }
}
