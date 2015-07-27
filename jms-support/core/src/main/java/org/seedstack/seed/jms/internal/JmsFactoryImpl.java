/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.jms.internal;

import io.nuun.kernel.api.plugin.PluginException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.utils.SeedBeanUtils;
import org.seedstack.seed.jms.spi.ConnectionDefinition;
import org.seedstack.seed.jms.spi.JmsErrorCodes;
import org.seedstack.seed.jms.spi.JmsExceptionHandler;
import org.seedstack.seed.jms.spi.JmsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Factory to create JMS objects.
 *
 * @author pierre.thirouin@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 */
class JmsFactoryImpl implements JmsFactory {
    public static final int DEFAULT_RECONNECTION_DELAY = 30000;

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsFactoryImpl.class);

    private final ConcurrentMap<String, ConnectionFactory> connectionFactoryMap = new ConcurrentHashMap<String, ConnectionFactory>();
    private final Map<String, Context> jndiContexts;
    private final String applicationName;
    private final Configuration jmsConfiguration;

    JmsFactoryImpl(String applicationName, Configuration jmsConfiguration, Map<String, Context> jndiContexts) {
        this.applicationName = applicationName;
        this.jmsConfiguration = jmsConfiguration;
        this.jndiContexts = jndiContexts;

        configureConnectionFactories();
    }

    @Override
    public Connection createConnection(ConnectionDefinition connectionDefinition) throws JMSException {
        Connection connection;

        if (connectionDefinition.isManaged()) {
            connection = new ManagedConnection(connectionDefinition, this);
            if (connectionDefinition.getExceptionListenerClass() != null) {
                LOGGER.debug("Setting exception listener {} on managed connection {}", connectionDefinition.getExceptionListenerClass(), connectionDefinition.getName());
                connection.setExceptionListener(new ExceptionListenerAdapter(connectionDefinition.getName()));
            }
        } else {
            connection = createRawConnection(connectionDefinition);
            if (!connectionDefinition.isJeeMode()) {
                if (connectionDefinition.getExceptionListenerClass() != null) {
                    LOGGER.debug("Setting exception listener {} on connection {}", connectionDefinition.getExceptionListenerClass(), connectionDefinition.getName());
                    connection.setExceptionListener(new ExceptionListenerAdapter(connectionDefinition.getName()));
                }
            }
        }

        return connection;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConnectionDefinition createConnectionDefinition(String connectionName, Configuration configuration, ConnectionFactory connectionFactory) {
        // Find connection factory if not given explicitly
        if (connectionFactory == null) {
            connectionFactory = connectionFactoryMap.get(configuration.getString("connection-factory"));

            if (connectionFactory == null) {
                throw SeedException.createNew(JmsErrorCodes.MISSING_CONNECTION_FACTORY).put("connectionName", connectionName);
            }
        }

        // Create exception listener
        String exceptionListenerClassName = configuration.getString("exception-listener");
        Class<? extends ExceptionListener> exceptionListener = null;
        if (StringUtils.isNotBlank(exceptionListenerClassName)) {
            try {
                exceptionListener = (Class<? extends ExceptionListener>) Class.forName(exceptionListenerClassName);
            } catch (Exception e) {
                throw new PluginException("Unable to load JMS ExceptionListener class " + exceptionListenerClassName, e);
            }
        }

        // Create exception handler
        String exceptionHandlerClassName = configuration.getString("exception-handler");
        Class<? extends JmsExceptionHandler> exceptionHandler = null;
        if (StringUtils.isNotBlank(exceptionHandlerClassName)) {
            try {
                exceptionHandler = (Class<? extends JmsExceptionHandler>) Class.forName(exceptionHandlerClassName);
            } catch (Exception e) {
                throw SeedException.wrap(e, JmsErrorCodes.UNABLE_TO_LOAD_CLASS).put("exceptionHandler", exceptionHandlerClassName);
            }
        }

        boolean jeeMode = configuration.getBoolean("jee-mode", false);
        boolean shouldSetClientId = configuration.getBoolean("set-client-id", !jeeMode);

        if (jeeMode && shouldSetClientId) {
            throw SeedException.createNew(JmsErrorCodes.CANNOT_SET_CLIENT_ID_IN_JEE_MODE).put(JmsPlugin.ERROR_CONNECTION_NAME, connectionName);
        }

        return new ConnectionDefinition(
                connectionName,
                connectionFactory,
                configuration.getBoolean("managed-connection", true),
                jeeMode,
                shouldSetClientId,
                configuration.getString("client-id", applicationName + "-" + connectionName),
                configuration.getString("user"),
                configuration.getString("password"),
                configuration.getInt("reconnection-delay", DEFAULT_RECONNECTION_DELAY),
                exceptionListener,
                exceptionHandler
        );
    }

    Connection createRawConnection(ConnectionDefinition connectionDefinition) throws JMSException {
        Connection connection;
        if (connectionDefinition.getUser() != null) {
            connection = connectionDefinition.getConnectionFactory().createConnection(connectionDefinition.getUser(), connectionDefinition.getPassword());
        } else {
            connection = connectionDefinition.getConnectionFactory().createConnection();
        }

        // client id is set here on raw connection
        if (connectionDefinition.isShouldSetClientId()) {
            LOGGER.debug("Setting client id as {} on connection {}", connectionDefinition.getClientId(), connectionDefinition.getName());
            connection.setClientID(connectionDefinition.getClientId());
        }

        return connection;
    }

    private void configureConnectionFactories() {
        String[] connectionFactories = jmsConfiguration.getStringArray("connection-factories");

        if (connectionFactories != null) {
            for (String connectionFactoryName : connectionFactories) {
                Configuration connectionFactoryConfiguration = jmsConfiguration.subset("connection-factory." + connectionFactoryName);

                String jndiName = connectionFactoryConfiguration.getString("jndi.name");
                String jndiContext = connectionFactoryConfiguration.getString("jndi.context", "default");
                String classname = connectionFactoryConfiguration.getString("vendor.class");

                Object connectionFactory;
                if (StringUtils.isNotBlank(jndiName)) {
                    connectionFactory = lookupConnectionFactory(connectionFactoryName, jndiContext, jndiName);
                } else if (StringUtils.isNotBlank(classname)) {
                    try {
                        connectionFactory = SeedBeanUtils.createFromConfiguration(connectionFactoryConfiguration, "vendor");

                    } catch (Exception e) {
                        throw SeedException.wrap(e, JmsErrorCodes.UNABLE_TO_CREATE_CONNECTION_FACTORY).put("connectionFactoryName", connectionFactoryName);
                    }
                } else {
                    throw SeedException.createNew(JmsErrorCodes.MISCONFIGURED_CONNECTION_FACTORY).put("connectionFactoryName", connectionFactoryName);
                }

                if (!(connectionFactory instanceof ConnectionFactory)) {
                    throw SeedException.createNew(JmsErrorCodes.UNRECOGNIZED_CONNECTION_FACTORY).put("classname", classname);
                }

                connectionFactoryMap.put(connectionFactoryName, (ConnectionFactory) connectionFactory);
            }
        }
    }

    private Object lookupConnectionFactory(String connectionFactoryName, String contextName, String jndiName) {
        try {
            if (this.jndiContexts == null || this.jndiContexts.isEmpty()) {
                throw SeedException.createNew(JmsErrorCodes.NO_JNDI_CONTEXT).put("connectionFactoryName", connectionFactoryName);
            }

            Context context = this.jndiContexts.get(contextName);
            if (context == null) {
                throw SeedException.createNew(JmsErrorCodes.MISSING_JNDI_CONTEXT).put("contextName", contextName).put("connectionFactoryName", connectionFactoryName);
            }

            return context.lookup(jndiName);
        } catch (NamingException e) {
            throw SeedException.wrap(e, JmsErrorCodes.JNDI_LOOKUP_ERROR).put("connectionFactoryName", connectionFactoryName);
        }
    }
}
