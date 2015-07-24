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

import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.kametic.specifications.Specification;
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.core.internal.jndi.JndiPlugin;
import org.seedstack.seed.core.utils.SeedCheckUtils;
import org.seedstack.seed.jms.api.DestinationType;
import org.seedstack.seed.jms.api.JmsMessageListener;
import org.seedstack.seed.jms.spi.ConnectionDefinition;
import org.seedstack.seed.jms.spi.JmsErrorCodes;
import org.seedstack.seed.jms.spi.JmsExceptionHandler;
import org.seedstack.seed.jms.spi.JmsFactory;
import org.seedstack.seed.jms.spi.MessageListenerDefinition;
import org.seedstack.seed.jms.spi.MessagePoller;
import org.seedstack.seed.transaction.internal.TransactionPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.naming.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This plugin provides JMS support through JNDI or plain configuration.
 *
 * @author emmanuel.vinel@mpsa.com
 * @author adrien.lauer@mpsa.com
 * @author redouane.loulou@ext.mpsa.com
 */
public class JmsPlugin extends AbstractPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(JmsPlugin.class);

    public static final String JMS_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.seed.jms";
    public static final String ERROR_CONNECTION_NAME = "connectionName";
    public static final String ERROR_MESSAGE_LISTENER_NAME = "messageListenerName";
    public static final String ERROR_DESTINATION_TYPE = "destinationType";

    @SuppressWarnings("unchecked")
    private final Specification<Class<?>> messageListenerSpec = and(classImplements(MessageListener.class), classAnnotatedWith(JmsMessageListener.class));
    private final Specification<Class<?>> exceptionListenerSpec = classImplements(ExceptionListener.class);
    private final Specification<Class<?>> exceptionHandlerSpec = classImplements(JmsExceptionHandler.class);

    private final ConcurrentMap<String, MessageListenerDefinition> messageListenerDefinitions = new ConcurrentHashMap<String, MessageListenerDefinition>();
    private final ConcurrentMap<String, ConnectionDefinition> connectionDefinitions = new ConcurrentHashMap<String, ConnectionDefinition>();

    private final ConcurrentMap<String, Connection> connections = new ConcurrentHashMap<String, Connection>();
    private final ConcurrentMap<String, MessagePoller> pollers = new ConcurrentHashMap<String, MessagePoller>();

    private final AtomicBoolean shouldStartConnections = new AtomicBoolean(false);

    private JmsFactory jmsFactory;
    private Application application;
    private Configuration jmsConfiguration;
    private TransactionPlugin transactionPlugin;
    private Map<String, Context> jndiContexts;

    @Override
    public String name() {
        return "seed-jms-plugin";
    }

    @Override
    public InitState init(InitContext initContext) {
        String applicationId = null;
        transactionPlugin = null;
        for (Plugin plugin : initContext.pluginsRequired()) {
            if (plugin instanceof ApplicationPlugin) {
                application = ((ApplicationPlugin) plugin).getApplication();
                jmsConfiguration = application.getConfiguration().subset(JmsPlugin.JMS_PLUGIN_CONFIGURATION_PREFIX);
                applicationId = ((ApplicationPlugin) plugin).getApplication().getId();
            } else if (plugin instanceof TransactionPlugin) {
                transactionPlugin = ((TransactionPlugin) plugin);
            } else if (plugin instanceof JndiPlugin) {
                jndiContexts = ((JndiPlugin) plugin).getJndiContexts();
            }
        }

        if (jmsConfiguration == null || applicationId == null) {
            throw SeedException.createNew(JmsErrorCodes.PLUGIN_NOT_FOUND).put("plugin", "application");
        }
        if (transactionPlugin == null) {
            throw SeedException.createNew(JmsErrorCodes.PLUGIN_NOT_FOUND).put("plugin", "transaction");
        }

        jmsFactory = new JmsFactoryImpl(applicationId, jmsConfiguration, jndiContexts);

        configureConnections(jmsConfiguration.getStringArray("connections"));

        configureMessageListeners(initContext.scannedTypesBySpecification().get(messageListenerSpec));

        return InitState.INITIALIZED;
    }

    @Override
    public void start(io.nuun.kernel.api.plugin.context.Context context) {
        shouldStartConnections.set(true);

        for (Map.Entry<String, Connection> connection : this.connections.entrySet()) {
            try {
                connection.getValue().start();
            } catch (JMSException e) {
                throw SeedException.wrap(e, JmsErrorCodes.UNABLE_TO_START_JMS_CONNECTION).put(ERROR_CONNECTION_NAME, connection.getKey());
            }
        }

        for (MessagePoller messagePoller : pollers.values()) {
            messagePoller.start();
        }
    }

    @Override
    public void stop() {
        shouldStartConnections.set(false);

        for (MessagePoller messagePoller : pollers.values()) {
            messagePoller.stop();
        }

        for (Map.Entry<String, Connection> connection : this.connections.entrySet()) {
            try {
                connection.getValue().close();
            } catch (JMSException e) {
                LOGGER.error("Unable to cleanly stop JMS connection " + connection.getKey(), e);
            }
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
        return classpathScanRequestBuilder()
                .specification(messageListenerSpec)
                .specification(exceptionListenerSpec)
                .specification(exceptionHandlerSpec)
                .build();
    }

    @Override
    public Object nativeUnitModule() {
        return new JmsModule(
                jmsFactory,
                connections,
                connectionDefinitions,
                messageListenerDefinitions,
                pollers.values()
        );
    }

    @SuppressWarnings("unchecked")
    private void configureConnections(String[] connectionNames) {
        for (String connectionName : connectionNames) {
            try {
                ConnectionDefinition connectionDefinition = jmsFactory.createConnectionDefinition(
                        connectionName,
                        jmsConfiguration.subset("connection." + connectionName),
                        null
                );

                registerConnection(jmsFactory.createConnection(connectionDefinition), connectionDefinition);
            } catch (JMSException e) {
                throw SeedException.wrap(e, JmsErrorCodes.UNABLE_TO_CREATE_JMS_CONNECTION).put(ERROR_CONNECTION_NAME, connectionName);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void configureMessageListeners(Collection<Class<?>> listenerCandidates) {
        for (Class<?> candidate : listenerCandidates) {
            if (MessageListener.class.isAssignableFrom(candidate)) {
                Class<? extends MessageListener> messageListenerClass = (Class<? extends MessageListener>) candidate;
                String messageListenerName = messageListenerClass.getCanonicalName();
                JmsMessageListener annotation = messageListenerClass.getAnnotation(JmsMessageListener.class);

                boolean isTransactional;
                try {
                    isTransactional = transactionPlugin.isTransactional(messageListenerClass.getMethod("onMessage", Message.class));
                } catch (NoSuchMethodException e) {
                    throw SeedException.wrap(e, JmsErrorCodes.UNEXPECTED_EXCEPTION);
                }

                Connection listenerConnection = connections.get(annotation.connection());

                if (listenerConnection == null) {
                    throw SeedException.createNew(JmsErrorCodes.MISSING_CONNECTION_FACTORY)
                            .put(ERROR_CONNECTION_NAME, annotation.connection())
                            .put(ERROR_MESSAGE_LISTENER_NAME, messageListenerName);
                }

                Session session;
                try {
                    session = listenerConnection.createSession(isTransactional, Session.AUTO_ACKNOWLEDGE);
                } catch (JMSException e) {
                    throw SeedException.wrap(e, JmsErrorCodes.UNABLE_TO_CREATE_SESSION)
                            .put(ERROR_CONNECTION_NAME, annotation.connection())
                            .put(ERROR_MESSAGE_LISTENER_NAME, messageListenerName);
                }

                Destination destination;
                DestinationType destinationType;

                if (!annotation.destinationTypeStr().isEmpty()) {
                    try {
                        destinationType = DestinationType.valueOf(application.substituteWithConfiguration(annotation.destinationTypeStr()));
                    } catch (IllegalArgumentException e) {
                        throw SeedException.wrap(e, JmsErrorCodes.UNKNOWN_DESTINATION_TYPE)
                                .put(ERROR_DESTINATION_TYPE, annotation.destinationTypeStr())
                                .put(ERROR_CONNECTION_NAME, annotation.connection())
                                .put(ERROR_MESSAGE_LISTENER_NAME, messageListenerName);
                    }
                } else {
                    destinationType = annotation.destinationType();
                }
                try {
                    switch (destinationType) {
                        case QUEUE:
                            destination = session.createQueue(application.substituteWithConfiguration(annotation.destinationName()));
                            break;
                        case TOPIC:
                            destination = session.createTopic(application.substituteWithConfiguration(annotation.destinationName()));
                            break;
                        default:
                            throw SeedException.createNew(JmsErrorCodes.UNKNOWN_DESTINATION_TYPE)
                                    .put(ERROR_CONNECTION_NAME, annotation.connection())
                                    .put(ERROR_MESSAGE_LISTENER_NAME, messageListenerName);
                    }
                } catch (JMSException e) {
                    throw SeedException.wrap(e, JmsErrorCodes.UNABLE_TO_CREATE_DESTINATION)
                            .put(ERROR_DESTINATION_TYPE, destinationType.name())
                            .put(ERROR_CONNECTION_NAME, annotation.connection())
                            .put(ERROR_MESSAGE_LISTENER_NAME, messageListenerName);
                }

                Class<? extends MessagePoller> messagePollerClass = null;
                if (annotation.poller().length > 0) {
                    messagePollerClass = annotation.poller()[0];
                }

                registerMessageListener(
                        new MessageListenerDefinition(
                                messageListenerName,
                                application.substituteWithConfiguration(annotation.connection()),
                                session,
                                destination,
                                application.substituteWithConfiguration(annotation.selector()),
                                messageListenerClass,
                                messagePollerClass
                        )
                );
            }
        }
    }

    private MessageConsumer createMessageConsumer(MessageListenerDefinition messageListenerDefinition) throws JMSException {
        LOGGER.debug("Creating JMS consumer for listener {}", messageListenerDefinition.getName());

        MessageConsumer consumer;
        Session session = messageListenerDefinition.getSession();

        if (StringUtils.isNotBlank(messageListenerDefinition.getSelector())) {
            consumer = session.createConsumer(messageListenerDefinition.getDestination(), messageListenerDefinition.getSelector());
        } else {
            consumer = session.createConsumer(messageListenerDefinition.getDestination());
        }

        MessagePoller messagePoller;
        if (messageListenerDefinition.getPoller() != null) {
            try {
                LOGGER.debug("Creating poller for JMS listener {}", messageListenerDefinition.getName());

                Connection connection = connections.get(messageListenerDefinition.getConnectionName());

                messagePoller = messageListenerDefinition.getPoller().newInstance();
                messagePoller.setSession(session);
                messagePoller.setMessageConsumer(consumer);
                messagePoller.setMessageListener(new MessageListenerAdapter(messageListenerDefinition.getName()));

                if (connection instanceof ManagedConnection) {
                    messagePoller.setExceptionListener((ExceptionListener) connection);
                } else {
                    messagePoller.setExceptionListener(connection.getExceptionListener());
                }
            } catch (Exception e) {
                throw SeedException.wrap(e, JmsErrorCodes.UNABLE_TO_CREATE_POLLER).put("pollerClass", messageListenerDefinition.getPoller());
            }

            pollers.put(messageListenerDefinition.getName(), messagePoller);
        } else {
            consumer.setMessageListener(new MessageListenerAdapter(messageListenerDefinition.getName()));
        }

        return consumer;
    }

    /**
     * Register an existing JMS connection to be managed by the JMS plugin.
     *
     * @param connection           the connection.
     * @param connectionDefinition the connection definition.
     */
    public void registerConnection(Connection connection, ConnectionDefinition connectionDefinition) {
        SeedCheckUtils.checkIfNotNull(connection);
        SeedCheckUtils.checkIfNotNull(connectionDefinition);

        if (this.connectionDefinitions.putIfAbsent(connectionDefinition.getName(), connectionDefinition) != null) {
            throw SeedException.createNew(JmsErrorCodes.DUPLICATE_CONNECTION_NAME).put(ERROR_CONNECTION_NAME, connectionDefinition.getName());
        }

        if (this.connections.putIfAbsent(connectionDefinition.getName(), connection) != null) {
            throw SeedException.createNew(JmsErrorCodes.DUPLICATE_CONNECTION_NAME).put(ERROR_CONNECTION_NAME, connectionDefinition.getName());
        }

        if (shouldStartConnections.get()) {
            try {
                connection.start();
            } catch (JMSException e) {
                throw SeedException.wrap(e, JmsErrorCodes.UNABLE_TO_START_JMS_CONNECTION).put(ERROR_CONNECTION_NAME, connectionDefinition.getName());
            }
        }
    }

    /**
     * Register a message listener definition to be managed by the JMS plugin.
     *
     * @param messageListenerDefinition the message listener definition.
     */
    public void registerMessageListener(MessageListenerDefinition messageListenerDefinition) {
        SeedCheckUtils.checkIfNotNull(messageListenerDefinition);

        ConnectionDefinition connectionDefinition = connectionDefinitions.get(messageListenerDefinition.getConnectionName());
        if (connectionDefinition.isJeeMode() && messageListenerDefinition.getPoller() == null) {
            throw SeedException.createNew(JmsErrorCodes.MESSAGE_POLLER_REQUIRED_IN_JEE_MODE)
                    .put(ERROR_CONNECTION_NAME, connectionDefinition.getName())
                    .put(ERROR_MESSAGE_LISTENER_NAME, messageListenerDefinition.getName());
        }

        try {
            createMessageConsumer(messageListenerDefinition);
        } catch (JMSException e) {
            throw SeedException.wrap(e, JmsErrorCodes.UNABLE_TO_CREATE_MESSAGE_CONSUMER).put(ERROR_MESSAGE_LISTENER_NAME, messageListenerDefinition.getName());
        }

        if (messageListenerDefinitions.putIfAbsent(messageListenerDefinition.getName(), messageListenerDefinition) != null) {
            throw SeedException.createNew(JmsErrorCodes.DUPLICATE_MESSAGE_LISTENER_DEFINITION_NAME).put(ERROR_MESSAGE_LISTENER_NAME, messageListenerDefinition.getName());
        }
    }

    /**
     * Retrieve a connection by name.
     *
     * @param name the name of the connection to retrieve.
     * @return the connection or null if it doesn't exists.
     */
    public Connection getConnection(String name) {
        return connections.get(name);
    }

    /**
     * Return the factory used to create JMS objects.
     *
     * @return the JMS factory.
     */
    public JmsFactory getJmsFactory() {
        return jmsFactory;
    }
}
