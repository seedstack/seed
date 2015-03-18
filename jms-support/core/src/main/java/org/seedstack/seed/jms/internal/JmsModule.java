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
import org.seedstack.seed.jms.spi.JmsExceptionHandler;
import org.seedstack.seed.jms.spi.MessageListenerDefinition;
import org.seedstack.seed.transaction.utils.TransactionalProxy;

import javax.jms.Connection;
import javax.jms.Session;
import java.util.Map;

class JmsModule extends AbstractModule {

    private final Map<String, MessageListenerDefinition> messageListenerDefinitions;
    private final Map<String, Connection> connectionMap;
    private final Map<String, Class<? extends JmsExceptionHandler>> jmsExceptionHandlerClasses;

    JmsModule(Map<String, Connection> connectionMap, Map<String, MessageListenerDefinition> messageListenerDefinitions,
              Map<String, Class<? extends JmsExceptionHandler>> jmsExceptionHandlerClasses) {
        this.connectionMap = connectionMap;
        this.messageListenerDefinitions = messageListenerDefinitions;
        this.jmsExceptionHandlerClasses = jmsExceptionHandlerClasses;
    }

    @Override
    protected void configure() {
        JmsSessionLink jmsSessionLink = new JmsSessionLink();
        bind(Session.class).toInstance(TransactionalProxy.create(Session.class, jmsSessionLink));

        for (Map.Entry<String, Connection> connection : connectionMap.entrySet()) {
            bindConnection(connection.getKey(), connection.getValue(), jmsSessionLink);
        }

        for (Map.Entry<String, MessageListenerDefinition> entry : messageListenerDefinitions.entrySet()) {
            bind(JmsListenerTransactionHandler.class)
                    .annotatedWith(Names.named(entry.getKey()))
                    .toInstance(new JmsListenerTransactionHandler(entry.getValue().getSession()));

            if (entry.getValue().getMessageListenerClass() != null) {
                bind(entry.getValue().getMessageListenerClass());
            }

            // already instantiated JMS listeners are left to be injected by those who constructed the instance
        }
    }

    private void bindConnection(String name, Connection connection, JmsSessionLink jmsSessionLink) {
        Class<? extends JmsExceptionHandler> connectionExceptionHandlerClass = jmsExceptionHandlerClasses.get(name);

        if (connectionExceptionHandlerClass != null) {
            bind(JmsExceptionHandler.class).annotatedWith(Names.named(name)).to(connectionExceptionHandlerClass);
        } else {
            bind(JmsExceptionHandler.class).annotatedWith(Names.named(name)).toProvider(Providers.<JmsExceptionHandler>of(null));
        }

        bind(Connection.class).annotatedWith(Names.named(name)).toInstance(connection);
        JmsTransactionHandler transactionHandler = new JmsTransactionHandler(jmsSessionLink, connection);
        bind(JmsTransactionHandler.class).annotatedWith(Names.named(name)).toInstance(transactionHandler);
    }
}
