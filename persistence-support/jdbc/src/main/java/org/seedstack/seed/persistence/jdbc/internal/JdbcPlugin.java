/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 17 févr. 2015
 */
package org.seedstack.seed.persistence.jdbc.internal;

import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.core.internal.jndi.JndiPlugin;
import org.seedstack.seed.metrics.internal.MetricsPlugin;
import org.seedstack.seed.persistence.jdbc.api.JdbcExceptionHandler;
import org.seedstack.seed.persistence.jdbc.internal.datasource.PlainDataSourceProvider;
import org.seedstack.seed.persistence.jdbc.spi.DataSourceProvider;
import org.seedstack.seed.transaction.internal.TransactionPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * JDBC support plugin
 */
public class JdbcPlugin extends AbstractPlugin {
    public static final String JDBC_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.seed.persistence.jdbc";
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcPlugin.class);

    private final Map<String, DataSource> dataSources = new HashMap<String, DataSource>();
    private final Map<String, Class<? extends JdbcExceptionHandler>> exceptionHandlerClasses = new HashMap<String, Class<? extends JdbcExceptionHandler>>();
    private final Map<Class<?>, String> registeredClasses = new HashMap<Class<?>, String>();

    @Override
    public String name() {
        return "seed-persistence-jdbc-plugin";
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState init(InitContext initContext) {
        Configuration jdbcConfiguration = null;
        TransactionPlugin transactionPlugin = null;
        JndiPlugin jndiPlugin = null;
        MetricsPlugin metricsPlugin = null;
        for (Plugin plugin : initContext.pluginsRequired()) {
            if (plugin instanceof ApplicationPlugin) {
                jdbcConfiguration = ((ApplicationPlugin) plugin).getApplication().getConfiguration().subset(JDBC_PLUGIN_CONFIGURATION_PREFIX);
            } else if (plugin instanceof TransactionPlugin) {
                transactionPlugin = ((TransactionPlugin) plugin);
            } else if (plugin instanceof JndiPlugin) {
                jndiPlugin = ((JndiPlugin) plugin);
            } else if (plugin instanceof MetricsPlugin) {
                metricsPlugin = ((MetricsPlugin) plugin);
            }
        }

        if (jdbcConfiguration == null || transactionPlugin == null || jndiPlugin == null) {
            throw new PluginException("Unsatisfied plugin dependencies, ApplicationPlugin, TransactionPlugin and JndiPlugin are required");
        }

        Map<String, Class<? extends DataSourceProvider>> dataSourceProviders = new HashMap<String, Class<? extends DataSourceProvider>>();
        for (Class<?> clazz : initContext.scannedSubTypesByParentClass().get(DataSourceProvider.class)) {
            dataSourceProviders.put(clazz.getSimpleName(), (Class<? extends DataSourceProvider>) clazz);
        }

        String[] datasourceNames = jdbcConfiguration.getStringArray("datasources");
        if (datasourceNames.length > 0) {
            for (String datasourceName : datasourceNames) {
                Configuration dataSourceConfig = jdbcConfiguration.subset("datasource." + datasourceName);
                DataSource dataSource;
                String dataSourceContextName = dataSourceConfig.getString("context");
                Context context;
                if(dataSourceContextName != null){
                	context = jndiPlugin.getJndiContexts().get(dataSourceContextName);
                	if(context == null){
                        throw new PluginException("Wrong context [" + dataSourceContextName + "] name for datasource " + dataSourceContextName);
                	}
                }else{
                	context = jndiPlugin.getJndiContexts().get("default");
                }
                String dataSourceJndiName = dataSourceConfig.getString("jndi-name");
                if (dataSourceJndiName != null) {
                    try {
                        dataSource = (DataSource) context.lookup(dataSourceJndiName);
                    } catch (NamingException e) {
                        throw new PluginException("Wrong JNDI name for datasource " + datasourceName, e);
                    }
                } else {
                    String dataSourceProviderName = dataSourceConfig.getString("provider", PlainDataSourceProvider.class.getSimpleName());
                    try {
                        Class<? extends DataSourceProvider> providerClass = dataSourceProviders.get(dataSourceProviderName);
                        if (providerClass == null) {
                            throw new PluginException("Could not find a matching DataSourceProvider for configured value : " + dataSourceProviderName);
                        }
                        DataSourceProvider provider = providerClass.newInstance();

                        if (metricsPlugin != null) {
                            provider.setHealthCheckRegistry(metricsPlugin.getHealthCheckRegistry());
                            provider.setMetricRegistry(metricsPlugin.getMetricRegistry());
                        }

                        Iterator<String> it = dataSourceConfig.getKeys("property");
                        Properties otherProperties = new Properties();
                        while (it.hasNext()) {
                            String name = it.next();
                            otherProperties.put(name.substring(9), dataSourceConfig.getString(name));
                        }
                        dataSource = provider.provideDataSource(dataSourceConfig.getString("driver"), dataSourceConfig.getString("url"),
                                dataSourceConfig.getString("user"), dataSourceConfig.getString("password"), otherProperties);
                    } catch (InstantiationException e) {
                        throw new PluginException("Unable to load class " + dataSourceProviderName, e);
                    } catch (IllegalAccessException e) {
                        throw new PluginException("Unable to load class " + dataSourceProviderName, e);
                    }
                }
                dataSources.put(datasourceName, dataSource);

                String exceptionHandler = dataSourceConfig.getString("exception-handler");
                if (exceptionHandler != null && !exceptionHandler.isEmpty()) {
                    try {
                        exceptionHandlerClasses.put(datasourceName, (Class<? extends JdbcExceptionHandler>) Class.forName(exceptionHandler));
                    } catch (Exception e) {
                        throw new PluginException("Unable to load class " + exceptionHandler, e);
                    }
                }
            }

            if (datasourceNames.length == 1) {
                JdbcTransactionMetadataResolver.defaultJdbc = datasourceNames[0];
            }
            transactionPlugin.registerTransactionHandler(JdbcTransactionHandler.class);
        } else {
            LOGGER.info("No datasource configured, JDBC support disabled");
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        plugins.add(TransactionPlugin.class);
        plugins.add(JndiPlugin.class);
        plugins.add(MetricsPlugin.class);
        return plugins;
    }

    @Override
    public Object nativeUnitModule() {
        return new JdbcModule(dataSources, exceptionHandlerClasses, registeredClasses);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().subtypeOf(DataSourceProvider.class).build();
    }

    /**
     * This method allows to automatically use a datasource for the given class when it asks for the injection of a connection.
     * 
     * @param dataSourceName the datasource to use
     * @param clazz the class requiring a connection
     */
    public void registerDataSourceForClass(Class<?> clazz, String dataSourceName) {
        if (!dataSources.containsKey(dataSourceName)) {
            throw new PluginException("DataSource [" + dataSourceName
                    + "] Does not exist. Make sure it corresponds to a DataSource declared under configuration " + JDBC_PLUGIN_CONFIGURATION_PREFIX
                    + ".datasources");
        }
        registeredClasses.put(clazz, dataSourceName);
    }

    /**
     * Provides the configured datasources by their names
     * 
     * @return a Map of Datasource indexed by their name.
     */
    public Map<String, DataSource> getDataSources() {
        return dataSources;
    }
}
