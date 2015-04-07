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
 * @author adrien.lauer@mpsa.com
 */
public class MessageListenerInstanceDefinition extends MessageListenerDefinition {
    private final MessageListener messageListener;

    /**
     * Creates a JMS message listener definition based on a MessageListener implementing class.
     *
     * @param name            the listener name
     * @param connectionName  the connection name that this listener is attached to.
     * @param session         the JMS session
     * @param destination     the JMS destination
     * @param selector        the message selector
     * @param messageListener the MessageListener instance
     * @param poller          an optional {@link MessagePoller} to retrieve messages via receive().
     */
    public MessageListenerInstanceDefinition(String name, String connectionName, Session session, Destination destination, String selector, MessageListener messageListener, Class<? extends MessagePoller> poller) {
        super(name, connectionName, session, destination, selector, messageListener.getClass(), poller);
        this.messageListener = messageListener;
    }

    public MessageListener getMessageListener() {
        return messageListener;
    }
}
