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
    private final Class<? extends MessageListener> messageListenerClass;
    private final MessageListener messageListener;
    private final String selector;
    private final Session session;
    private Destination destination;

    /**
     * Creates a JMS message listener definition based on a MessageListener implementing class.
     *
     * @param messageListenerClass the class implementing MessageListener.
     * @param session              the JMS session.
     * @param destination          the destination definition
     * @param selector             the message selector;
     */
    public MessageListenerDefinition(Class<? extends MessageListener> messageListenerClass, Session session, Destination destination, String selector) {
        this.messageListenerClass = messageListenerClass;
        this.messageListener = null;
        this.session = session;
        this.destination = destination;
        this.selector = selector;
    }

    /**
     * Creates a JMS message listener definition based on an already created MessageListener instance.
     *
     * @param messageListener the MessageListener instance.
     * @param session         the JMS session.
     * @param destination     the destination definition
     * @param selector        the message selector;
     */
    public MessageListenerDefinition(MessageListener messageListener, Session session, Destination destination, String selector) {
        this.messageListenerClass = null;
        this.messageListener = messageListener;
        this.session = session;
        this.destination = destination;
        this.selector = selector;
    }

    /**
     * @return the JMS session.
     */
    public Session getSession() {
        return session;
    }

    /**
     * @return the destination definition
     */
    public Destination getDestination() {
        return destination;
    }

    /**
     * @return the message selector.
     */
    public String getSelector() {
        return selector;
    }

    /**
     * @return the MessageListener implementing class, null if it is an instance-based definition.
     */
    public Class<? extends MessageListener> getMessageListenerClass() {
        return messageListenerClass;
    }

    /**
     * @return the MessageListener instance, null if it is an class-based definition.
     */
    public MessageListener getMessageListener() {
        return messageListener;
    }
}
