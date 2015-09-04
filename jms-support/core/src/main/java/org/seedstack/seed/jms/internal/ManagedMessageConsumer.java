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

import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.utils.SeedCheckUtils;
import org.seedstack.seed.jms.spi.JmsErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This session is a facade of a jms messageConsumer. It allows the reconnection mechanism.
 *
 * @author pierre.thirouin@ext.mpsa.com
 *         06/11/2014
 */
class ManagedMessageConsumer implements MessageConsumer {
    private static final Logger logger = LoggerFactory.getLogger(ManagedMessageConsumer.class);

    private MessageListener messageListener;
    private MessageConsumer messageConsumer;

    private final Destination destination;
    private final String messageSelector;
    private final boolean noLocal;
    private final boolean polling;
    protected final ReentrantReadWriteLock messageConsumerLock = new ReentrantReadWriteLock();

    ManagedMessageConsumer(MessageConsumer messageConsumer, Destination destination, @Nullable String messageSelector, boolean noLocal, boolean polling) {
        SeedCheckUtils.checkIfNotNull(messageConsumer);
        SeedCheckUtils.checkIfNotNull(destination);

        this.messageConsumer = messageConsumer;
        this.messageSelector = messageSelector;
        this.destination = destination;
        this.noLocal = noLocal;
        this.polling = polling;
    }

    void refresh(Session session) {
        messageConsumerLock.writeLock().lock();
        try {
            // Create a new messageConsumer
            if (this.noLocal) {
                messageConsumer = session.createConsumer(destination, messageSelector, true);
            } else if (messageSelector != null && !"".equals(messageSelector)) {
                messageConsumer = session.createConsumer(destination, messageSelector);
            } else {
                messageConsumer = session.createConsumer(destination);
            }

            // Refresh the message listener if it exists
            if (messageListener != null && !this.polling) {
                messageConsumer.setMessageListener(messageListener);
            }
        } catch (JMSException e) {
            throw SeedException.wrap(e, JmsErrorCodes.INITIALIZATION_EXCEPTION);
        } finally {
            messageConsumerLock.writeLock().unlock();
        }
    }

    void reset() {
        messageConsumerLock.writeLock().lock();
        try {
            logger.trace("Resetting message consumer");
            messageConsumer = null;
        } finally {
            messageConsumerLock.writeLock().unlock();
        }
    }

    private MessageConsumer getMessageConsumer() throws JMSException {
        messageConsumerLock.readLock().lock();
        try {
            if (messageConsumer == null) {
                throw new JMSException("Attempt to use a message consumer during connection refresh");
            }
            return messageConsumer;
        } finally {
            messageConsumerLock.readLock().unlock();
        }
    }

    @Override
    public String getMessageSelector() throws JMSException {
        return getMessageConsumer().getMessageSelector();
    }

    @Override
    public Message receiveNoWait() throws JMSException {
        return getMessageConsumer().receiveNoWait();
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        return getMessageConsumer().getMessageListener();
    }

    @Override
    public void close() throws JMSException {
        getMessageConsumer().close();
    }

    @Override
    public Message receive(long timeout) throws JMSException {
        return getMessageConsumer().receive(timeout);
    }

    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        if (!polling) {
            getMessageConsumer().setMessageListener(listener);
        }
        messageListener = listener;
    }

    @Override
    public Message receive() throws JMSException {
        return getMessageConsumer().receive();
    }
}
