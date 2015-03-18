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

import com.google.inject.Injector;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.kametic.specifications.Specification;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.core.internal.jndi.JndiPlugin;
import org.seedstack.seed.core.utils.SeedBeanUtils;
import org.seedstack.seed.core.utils.SeedCheckUtils;
import org.seedstack.seed.jms.api.JmsMessageListener;
import org.seedstack.seed.jms.spi.ConnectionDefinition;
import org.seedstack.seed.jms.spi.JmsExceptionHandler;
import org.seedstack.seed.jms.spi.MessageListenerDefinition;
import org.seedstack.seed.transaction.internal.TransactionPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This plugin provides JMS support through JNDI or plain configuration.
 *
 * @author emmanuel.vinel@mpsa.com
 * @author adrien.lauer@mpsa.com
 */
public class JmsPlugin extends AbstractPlugin {
    public static final String JMS_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.seed.jms";
    public static final int DEFAULT_RECONNECTION_DELAY = 30000;

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsPlugin.class);

    @SuppressWarnings("unchecked")
    private final Specification<Class<?>> messageListenerSpecs = and(classImplements(MessageListener.class), classAnnotatedWith(JmsMessageListener.class));

    private final ConcurrentMap<String, Connection> connectionMap = new ConcurrentHashMap<String, Connection>();

    private final ConcurrentMap<String, MessageListenerDefinition> messageListenerDefinitions = new ConcurrentHashMap<String, MessageListenerDefinition>();

    private final Map<String, MessageConsumer> listenerConsumers = new HashMap<String, MessageConsumer>();

    private final Map<String, Class<? extends JmsExceptionHandler>> jmsExceptionHandlerClasses = new HashMap<String, Class<? extends JmsExceptionHandler>>();

    private InternalConnectionFactory internalConnectionFactory;

    private Map<String, ConnectionFactory> connectionFactoryMap;

    private TransactionPlugin transactionPlugin;

    private Map<String, Context> jndiContexts;

    private Collection<Class<?>> listenerCandidates;

    @Inject
    Injector injector;

    @Override
    public String name() {
        return "seed-jms-plugin";
    }

    @Override
    public InitState init(InitContext initContext) {
        String applicationId = null;
        Configuration jmsConfiguration = null;
        transactionPlugin = null;
        for (Plugin plugin : initContext.pluginsRequired()) {
            if (plugin instanceof ApplicationPlugin) {
                jmsConfiguration = ((ApplicationPlugin) plugin).getApplication().getConfiguration().subset(JmsPlugin.JMS_PLUGIN_CONFIGURATION_PREFIX);
                applicationId = ((ApplicationPlugin) plugin).getApplication().getId();
            } else if (plugin instanceof TransactionPlugin) {
                transactionPlugin = ((TransactionPlugin) plugin);
            } else if (plugin instanceof JndiPlugin) {
                jndiContexts = ((JndiPlugin) plugin).getJndiContexts();
            }
        }

        if (jmsConfiguration == null || applicationId == null) {
            throw SeedException.createNew(SeedJmsErrorCodes.PLUGIN_NOT_FOUND).put("plugin", "application");
        }
        if (transactionPlugin == null) {
            throw SeedException.createNew(SeedJmsErrorCodes.PLUGIN_NOT_FOUND).put("plugin", "transaction");
        }

        if (jmsConfiguration.isEmpty()) {
            LOGGER.info("No JMS configuration found, JMS support disabled");
            return InitState.INITIALIZED;
        }

        internalConnectionFactory = new InternalConnectionFactory(applicationId);

        connectionFactoryMap = configureConnectionFactories(jmsConfiguration);

        configureConnections(jmsConfiguration, connectionFactoryMap);

        listenerCandidates = initContext.scannedTypesBySpecification().get(messageListenerSpecs);

        configureMessageListener();

        return InitState.INITIALIZED;
    }

    @Override
    public void start(io.nuun.kernel.api.plugin.context.Context context) {
        try {
            for (Map.Entry<String, MessageListenerDefinition> entry : messageListenerDefinitions.entrySet()) {
                MessageListenerDefinition definition = entry.getValue();
                MessageConsumer consumer;

                if (StringUtils.isNotBlank(definition.getSelector())) {
                    consumer = definition.getSession().createConsumer(definition.getDestination(), definition.getSelector());
                } else {
                    consumer = definition.getSession().createConsumer(definition.getDestination());
                }

                if (definition.getMessageListener() != null) {
                    consumer.setMessageListener(definition.getMessageListener());
                } else {
                    consumer.setMessageListener(injector.getInstance(definition.getMessageListenerClass()));
                }

                listenerConsumers.put(entry.getKey(), consumer);
            }
        } catch (JMSException e) {
            throw SeedException.wrap(e, SeedJmsErrorCodes.INITIALIZATION_EXCEPTION);
        }

        for (Map.Entry<String, Connection> connection : connectionMap.entrySet()) {
            try {
                connection.getValue().start();
            } catch (JMSException e) {
                throw SeedException.wrap(e, SeedJmsErrorCodes.UNABLE_TO_START_JMS_CONNECTION).put("connectionName", connection.getKey());
            }
        }
    }

    @Override
    public void stop() {
        for (Map.Entry<String, MessageConsumer> listenerConsumer : listenerConsumers.entrySet()) {
            try {
                listenerConsumer.getValue().close();
                LOGGER.info("Closed JMS consumer for listener " + listenerConsumer.getKey());
            } catch (JMSException e) {
                LOGGER.error("Unable to cleanly close JMS consumer for listener " + listenerConsumer.getKey(), e);
            }
        }

        for (Map.Entry<String, Connection> connection : connectionMap.entrySet()) {
            try {
                connection.getValue().close();
            } catch (JMSException e) {
                LOGGER.error("Unable to cleanly stop JMS connection " + connection.getKey(), e);
            }
        }
    }

    private Map<String, ConnectionFactory> configureConnectionFactories(Configuration jmsConfiguration) {
        Map<String, ConnectionFactory> connectionFactoryMap = new HashMap<String, ConnectionFactory>();
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
                        throw SeedException.wrap(e, SeedJmsErrorCodes.UNABLE_TO_CREATE_CONNECTION_FACTORY).put("connectionFactoryName", connectionFactoryName);
                    }
                } else {
                    throw SeedException.createNew(SeedJmsErrorCodes.MISCONFIGURED_CONNECTION_FACTORY).put("connectionFactoryName", connectionFactoryName);
                }

                if (!(connectionFactory instanceof ConnectionFactory)) {
                    throw SeedException.createNew(SeedJmsErrorCodes.UNRECOGNIZED_CONNECTION_FACTORY).put("classname", classname);
                }

                connectionFactoryMap.put(connectionFactoryName, (ConnectionFactory) connectionFactory);

            }
        }
        return connectionFactoryMap;
    }

    @SuppressWarnings("unchecked")
    private void configureConnections(Configuration jmsConfiguration, Map<String, ConnectionFactory> connectionFactoryMap) {
        String[] connections = jmsConfiguration.getStringArray("connections");

        if (connections != null) {
            for (String connectionName : connections) {
                Configuration connectionConfiguration = jmsConfiguration.subset("connection." + connectionName);
                ConnectionFactory connectionFactory = connectionFactoryMap.get(connectionConfiguration.getString("connection-factory"));

                if (connectionFactory == null) {
                    throw SeedException.createNew(SeedJmsErrorCodes.MISSING_CONNECTION_FACTORY).put("connectionName", connectionName);
                }

                // Create connection
                Connection connection;
                try {
                    connection = internalConnectionFactory.create(
                        connectionFactory,
                        createConnectionDefinitionFromConfiguration(connectionConfiguration),
                        connectionName
                    );
                } catch (JMSException e) {
                    throw SeedException.wrap(e, SeedJmsErrorCodes.UNABLE_TO_CREATE_JMS_CONNECTION).put("connectionName", connectionName);
                }

                // Create exception handler
                String exceptionHandler = connectionConfiguration.getString("exception-handler");
                if (exceptionHandler != null && !exceptionHandler.isEmpty()) {
                    try {
                        jmsExceptionHandlerClasses.put(connectionName, (Class<? extends JmsExceptionHandler>) Class.forName(exceptionHandler));
                    } catch (Exception e) {
                        throw SeedException.wrap(e, SeedJmsErrorCodes.UNABLE_TO_LOAD_CLASS).put("exceptionHandler", exceptionHandler);
                    }
                }

                registerConnection(connectionName, connection);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void configureMessageListener() {
        for (Class<?> candidate : listenerCandidates) {
            if (MessageListener.class.isAssignableFrom(candidate)) {
                Class<? extends MessageListener> messageListenerClass = (Class<? extends MessageListener>) candidate;
                JmsMessageListener annotation = messageListenerClass.getAnnotation(JmsMessageListener.class);

                boolean isTransactional;
                try {
                    isTransactional = transactionPlugin.isTransactional(messageListenerClass.getMethod("onMessage", Message.class));
                } catch (NoSuchMethodException e) {
                    throw SeedException.wrap(e, SeedJmsErrorCodes.UNEXPECTED_EXCEPTION);
                }
                LOGGER.info("Creating JMS session for listener " + messageListenerClass.getCanonicalName()
                        + (isTransactional ? " with transaction support" : " without transaction support"));

                Connection listenerConnection = connectionMap.get(annotation.connection()); // NOSONAR
                if (listenerConnection == null) {
                    throw SeedException.createNew(SeedJmsErrorCodes.MISSING_CONNECTION_FACTORY)
                            .put("connectionName", annotation.connection())
                            .put("messageListenerClass", messageListenerClass);
                }

                Session session;
                try {
                    session = listenerConnection.createSession(isTransactional, Session.AUTO_ACKNOWLEDGE);
                } catch (JMSException e) {
                    throw SeedException.wrap(e, SeedJmsErrorCodes.UNABLE_TO_CREATE_SESSION_FOR_LISTENER).put("messageListenerClass", messageListenerClass);
                }

                Destination destination;
                try {
                    switch (annotation.destinationType()) {
                        case QUEUE:
                            destination = session.createQueue(annotation.destinationName());
                            break;
                        case TOPIC:
                            destination = session.createTopic(annotation.destinationName());
                            break;
                        default:
                            throw SeedException.createNew(SeedJmsErrorCodes.UNKNOWN_DESTINATION_TYPE).put("destinationType", annotation.destinationType());
                    }
                } catch (JMSException e) {
                    throw SeedException.wrap(e, SeedJmsErrorCodes.UNABLE_TO_CREATE_DESTINATION_FOR_LISTENER).put("messageListenerClass", messageListenerClass);
                }

                registerMessageListener(
                    messageListenerClass.getCanonicalName(),
                    new MessageListenerDefinition(
                            messageListenerClass,
                            session,
                            destination,
                            annotation.selector()
                    )
                );
            }
        }
    }

    private Object lookupConnectionFactory(String connectionFactoryName, String contextName, String jndiName) {
        try {
            if (this.jndiContexts == null || this.jndiContexts.isEmpty()) {
                throw SeedException.createNew(SeedJmsErrorCodes.NO_JNDI_CONTEXT).put("connectionFactoryName", connectionFactoryName);
            }

            Context context = this.jndiContexts.get(contextName);
            if (context == null) {
                throw SeedException.createNew(SeedJmsErrorCodes.MISSING_JNDI_CONTEXT).put("contextName", contextName).put("connectionFactoryName", connectionFactoryName);
            }

            return context.lookup(jndiName);
        } catch (NamingException e) {
            throw SeedException.wrap(e, SeedJmsErrorCodes.JNDI_LOOKUP_ERROR).put("connectionFactoryName", connectionFactoryName);
        }
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        plugins.add(TransactionPlugin.class);
        plugins.add(JndiPlugin.class);
        return plugins;
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().specification(messageListenerSpecs).build();
    }

    @Override
    public Object nativeUnitModule() {
        return new JmsModule(connectionMap, messageListenerDefinitions, jmsExceptionHandlerClasses);
    }

    /**
     * Register an existing JMS connection to be started and stopped by the JMS plugin.
     *
     * @param name       the connection name.
     * @param connection the connection instance.
     */
    public void registerConnection(String name, Connection connection) {
        SeedCheckUtils.checkIfNotNull(connection);
        if (connectionMap.putIfAbsent(name, connection) != null) {
            throw SeedException.createNew(SeedJmsErrorCodes.DUPLICATE_CONNECTION_NAME).put("name", name);
        }
    }

    /**
     * Register a message listener definition to be started and stopped by the JMS plugin.
     *
     * @param name                      the message listener name.
     * @param messageListenerDefinition the message listener definition.
     */
    public void registerMessageListener(String name, MessageListenerDefinition messageListenerDefinition) {
        SeedCheckUtils.checkIfNotNull(messageListenerDefinition);
        if (messageListenerDefinitions.putIfAbsent(name, messageListenerDefinition) != null) {
            throw SeedException.createNew(SeedJmsErrorCodes.DUPLICATE_MESSAGE_LISTENER_DEFINITION_NAME).put("name", name);
        }
    }

    /**
     * Retrieve a connection factory by name.
     *
     * @param name the name of the connection factory to retrieve.
     * @return the connection factory or null if it doesn't exists.
     */
    public ConnectionFactory getConnectionFactory(String name) {
        return connectionFactoryMap.get(name);
    }

    /**
     * Retrieve a connection by name.
     *
     * @param name the name of the connection to retrieve.
     * @return the connection or null if it doesn't exists.
     */
    public Connection getConnection(String name) {
        return connectionMap.get(name);
    }

    /**
     * Create a connection.
     *
     * @param connectionFactory    the connection factory
     * @param connectionDefinition the connection definition
     * @param connectionName       a unique connection name (if null a name will be generated)
     * @return a jms connection
     * @throws JMSException the connection can't be created
     */
    public Connection createConnection(ConnectionFactory connectionFactory, ConnectionDefinition connectionDefinition, String connectionName) throws JMSException {
        return internalConnectionFactory.create(connectionFactory, connectionDefinition, connectionName);
    }

    public ConnectionDefinition createConnectionDefinitionFromConfiguration(Configuration givenConfiguration) {
        Configuration configuration = givenConfiguration;
        if (configuration == null) {
            configuration = new BaseConfiguration();
        }

        return new ConnectionDefinition(
            configuration.getBoolean("managed-connection", true),
            configuration.getBoolean("set-client-id", true),
            configuration.getString("client-id"),
            configuration.getString("user"),
            configuration.getString("password"),
            configuration.getInt("reconnection-delay", DEFAULT_RECONNECTION_DELAY)
        );
    }
}
