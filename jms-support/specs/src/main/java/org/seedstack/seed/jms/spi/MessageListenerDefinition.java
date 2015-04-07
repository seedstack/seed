/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.jms.spi;

import javax.jms.Destination;
import javax.jms.MessageListener;
import javax.jms.Session;

/**
 * This class holds all information to dynamically register a JMS listener.
 *
 * @author adrien.lauer@mpsa.com
 */
public class MessageListenerDefinition {
    private final String name;
    private final String connectionName;
    private final Class<? extends MessageListener> messageListenerClass;

    private final Session session;
    private final Destination destination;
    private final String selector;
    private final Class<? extends MessagePoller> poller;

    /**
     * Creates a JMS message listener definition based on a MessageListener implementing class.
     * @param name                 the name of the message listener definition.
     * @param connectionName  the connection name that this listener is attached to.
     * @param session              the JMS session
     * @param destination          the JMS destination
     * @param selector             the message selector
     * @param messageListenerClass the class implementing MessageListener
     * @param poller               an optional {@link MessagePoller} to retrieve messages via receive().
     */
    public MessageListenerDefinition(String name, String connectionName, Session session, Destination destination, String selector, Class<? extends MessageListener> messageListenerClass, Class<? extends MessagePoller> poller) {
        this.name = name;
        this.connectionName = connectionName;
        this.session = session;
        this.destination = destination;
        this.selector = selector;
        this.messageListenerClass = messageListenerClass;
        this.poller = poller;
    }

    public String getName() {
        return name;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public Session getSession() {
        return session;
    }

    public Class<? extends MessageListener> getMessageListenerClass() {
        return messageListenerClass;
    }

    public Destination getDestination() {
        return destination;
    }

    public String getSelector() {
        return selector;
    }

    public Class<? extends MessagePoller> getPoller() {
        return poller;
    }
}
