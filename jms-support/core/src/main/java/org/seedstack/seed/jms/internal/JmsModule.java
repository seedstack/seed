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


import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import org.seedstack.seed.jms.spi.ConnectionDefinition;
import org.seedstack.seed.jms.spi.JmsExceptionHandler;
import org.seedstack.seed.jms.spi.JmsFactory;
import org.seedstack.seed.jms.spi.MessageListenerDefinition;
import org.seedstack.seed.jms.spi.MessageListenerInstanceDefinition;
import org.seedstack.seed.jms.spi.MessagePoller;
import org.seedstack.seed.transaction.utils.TransactionalProxy;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.MessageListener;
import javax.jms.Session;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

class JmsModule extends AbstractModule {
    private final JmsFactory jmsFactory;
    private final Map<String, Connection> connections;
    private final Map<String, MessageListenerDefinition> messageListenerDefinitions;
    private final Map<String, ConnectionDefinition> connectionDefinitions;
    private final Collection<MessagePoller> pollers;

    public JmsModule(JmsFactory jmsFactory, ConcurrentMap<String, Connection> connections, ConcurrentMap<String, ConnectionDefinition> connectionDefinitions, Map<String, MessageListenerDefinition> messageListenerDefinitions, Collection<MessagePoller> pollers) {
        this.jmsFactory = jmsFactory;
        this.connections = connections;
        this.connectionDefinitions = connectionDefinitions;
        this.messageListenerDefinitions = messageListenerDefinitions;
        this.pollers = pollers;
    }

    @Override
    protected void configure() {
        requestStaticInjection(ExceptionListenerAdapter.class);
        requestStaticInjection(MessageListenerAdapter.class);

        bind(JmsFactory.class).toInstance(jmsFactory);
        requestInjection(jmsFactory);

        JmsSessionLink jmsSessionLink = new JmsSessionLink();
        bind(Session.class).toInstance(TransactionalProxy.create(Session.class, jmsSessionLink));

        for (Map.Entry<String, Connection> entry : connections.entrySet()) {
            bindConnection(connectionDefinitions.get(entry.getKey()), entry.getValue(), jmsSessionLink);
        }

        for (Map.Entry<String, MessageListenerDefinition> entry : messageListenerDefinitions.entrySet()) {
            bindMessageListener(entry.getValue());
        }

        for (MessagePoller poller : pollers) {
            requestInjection(poller);
        }
    }

    private void bindMessageListener(MessageListenerDefinition messageListenerDefinition) {
        String name = messageListenerDefinition.getName();

        bind(JmsListenerTransactionHandler.class)
                .annotatedWith(Names.named(name))
                .toInstance(new JmsListenerTransactionHandler(messageListenerDefinition.getSession()));

        if (messageListenerDefinition instanceof MessageListenerInstanceDefinition) {
            MessageListener messageListener = ((MessageListenerInstanceDefinition) messageListenerDefinition).getMessageListener();
            bind(MessageListener.class).annotatedWith(Names.named(name)).toInstance(messageListener);
        } else {
            bind(MessageListener.class).annotatedWith(Names.named(name)).to(messageListenerDefinition.getMessageListenerClass());
        }
    }


    private void bindConnection(ConnectionDefinition connectionDefinition, Connection connection, JmsSessionLink jmsSessionLink) {
        String name = connectionDefinition.getName();

        Class<? extends JmsExceptionHandler> jmsExceptionHandlerClass = connectionDefinition.getJmsExceptionHandlerClass();
        if (jmsExceptionHandlerClass != null) {
            bind(JmsExceptionHandler.class).annotatedWith(Names.named(name)).to(jmsExceptionHandlerClass);
        } else {
            bind(JmsExceptionHandler.class).annotatedWith(Names.named(name)).toProvider(Providers.<JmsExceptionHandler>of(null));
        }

        if (connectionDefinition.getExceptionListenerClass() != null) {
            bind(ExceptionListener.class).annotatedWith(Names.named(name)).to(connectionDefinition.getExceptionListenerClass());
        }

        bind(Connection.class).annotatedWith(Names.named(name)).toInstance(connection);

        JmsTransactionHandler transactionHandler = new JmsTransactionHandler(jmsSessionLink, connection);
        bind(JmsTransactionHandler.class).annotatedWith(Names.named(name)).toInstance(transactionHandler);
    }
}
