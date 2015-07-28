/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.jdbc.internal;

import io.nuun.kernel.api.plugin.PluginException;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.persistence.jdbc.api.JdbcErrorCode;
import org.seedstack.seed.persistence.jdbc.api.JdbcExceptionHandler;
import org.seedstack.seed.persistence.jdbc.internal.datasource.PlainDataSourceProvider;
import org.seedstack.seed.persistence.jdbc.spi.DataSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.*;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
class DataSourceDefinitionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceDefinitionFactory.class);

    private String[] dataSourceNames;
    private Configuration jdbcConfiguration;

    public DataSourceDefinitionFactory(Configuration jdbcConfiguration) {
        this.jdbcConfiguration = jdbcConfiguration;
        this.dataSourceNames = jdbcConfiguration.getStringArray("datasources");
    }

    Map<String, DataSourceDefinition> createDataSourceDefinitions(Map<String, Context> jndiContext, Collection<Class<?>> dataSourceProviderClasses) {
        // if there is no datasource configured do nothing
        if (dataSourceNames.length == 0) {
            LOGGER.info("No datasource configured, JDBC support disabled");
            return new HashMap<String, DataSourceDefinition>();
        }

        Map<String, Class<? extends DataSourceProvider>> dataSourceProviderMap = dataSourceProviderByClassName(dataSourceProviderClasses);

        final Map<String, DataSourceDefinition> dataSourceDefinitions = new HashMap<String, DataSourceDefinition>();

        for (String dataSourceName : dataSourceNames) {
            DataSourceDefinition dataSourceDefinition = createDataSourceDefinition(jndiContext, dataSourceProviderMap, dataSourceName);
            dataSourceDefinitions.put(dataSourceName, dataSourceDefinition);
        }
        return dataSourceDefinitions;
    }

    private DataSourceDefinition createDataSourceDefinition(Map<String, Context> jndiContext, Map<String, Class<? extends DataSourceProvider>> dataSourceProviderMap, String dataSourceName) {
        Configuration dataSourceConfig = jdbcConfiguration.subset("datasource." + dataSourceName);
        if (dataSourceConfig.isEmpty()) {
            throw SeedException.createNew(JdbcErrorCode.MISSING_DATASOURCE_CONFIG).put("name", dataSourceName);
        }
        DataSourceDefinition dataSourceDefinition;

        String dataSourceJndiName = dataSourceConfig.getString("jndi-name");
        if (dataSourceJndiName != null) {
            dataSourceDefinition = createJndiDataSource(jndiContext, dataSourceName, dataSourceConfig, dataSourceJndiName);
        } else {
            dataSourceDefinition = createDataSource(dataSourceProviderMap, dataSourceName, dataSourceConfig);
        }

        String exceptionHandler = dataSourceConfig.getString("exception-handler");
        if (exceptionHandler != null && !exceptionHandler.isEmpty()) {
            try {
                //noinspection unchecked
                dataSourceDefinition.setJdbcExceptionHandler((Class<? extends JdbcExceptionHandler>) Class.forName(exceptionHandler));
            } catch (Exception e) {
                throw new PluginException("Unable to load class " + exceptionHandler, e);
            }
        }
        return dataSourceDefinition;
    }

    private DataSourceDefinition createDataSource(Map<String, Class<? extends DataSourceProvider>> dataSourceProviderClasses, String datasourceName, Configuration dataSourceConfig) {
        DataSourceDefinition dataSourceDefinition = new DataSourceDefinition(datasourceName);
        String dataSourceProviderName = dataSourceConfig.getString("provider", PlainDataSourceProvider.class.getSimpleName());
        Class<? extends DataSourceProvider> providerClass = dataSourceProviderClasses.get(dataSourceProviderName);
        if (providerClass == null) {
            throw new PluginException("Could not find a matching DataSourceProvider for configured value: " + dataSourceProviderName);
        }
        DataSourceProvider provider;
        try {
            provider = providerClass.newInstance();
        } catch (Exception e) {
            throw new PluginException("Unable to load class " + dataSourceProviderName, e);
        }

        Iterator<String> it = dataSourceConfig.getKeys("property");
        Properties otherProperties = new Properties();
        while (it.hasNext()) {
            String name = it.next();
            otherProperties.put(name.substring(9), dataSourceConfig.getString(name));
        }

        dataSourceDefinition.setDataSource(provider.provide(
                dataSourceConfig.getString("driver"),
                dataSourceConfig.getString("url"),
                dataSourceConfig.getString("user"),
                dataSourceConfig.getString("password"),
                otherProperties));

        dataSourceDefinition.setDataSourceProvider(provider);
        return dataSourceDefinition;
    }

    private DataSourceDefinition createJndiDataSource(Map<String, Context> jndiContext, String datasourceName, Configuration dataSourceConfig, String dataSourceJndiName) {
        String dataSourceContextName = dataSourceConfig.getString("context");
        Context context;
        if (dataSourceContextName != null) {
            context = jndiContext.get(dataSourceContextName);
            if (context == null) {
                throw new PluginException("Wrong context [" + dataSourceContextName + "] name for datasource " + dataSourceContextName);
            }
        } else {
            context = jndiContext.get("default");
        }

        DataSource dataSource;
        try {
            dataSource = (DataSource) context.lookup(dataSourceJndiName);
        } catch (NamingException e) {
            throw new PluginException("Wrong JNDI name for datasource " + datasourceName, e);
        }
        return new DataSourceDefinition(datasourceName, dataSource);
    }

    private Map<String, Class<? extends DataSourceProvider>> dataSourceProviderByClassName(Collection<Class<?>> dataSourceProviderClasses) {
        Map<String, Class<? extends DataSourceProvider>> dataSourceProviderMap = new HashMap<String, Class<? extends DataSourceProvider>>();
        for (Class<?> clazz : dataSourceProviderClasses) {
            //noinspection unchecked
            dataSourceProviderMap.put(clazz.getSimpleName(), (Class<? extends DataSourceProvider>) clazz);
        }
        return dataSourceProviderMap;
    }
}
