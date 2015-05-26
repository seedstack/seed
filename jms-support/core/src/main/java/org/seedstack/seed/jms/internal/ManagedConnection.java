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

import org.seedstack.seed.core.utils.SeedCheckUtils;
import org.seedstack.seed.jms.spi.ConnectionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This connection is a facade to the actual jms connection. It provides the reconnection mechanism.
 *
 * @author pierre.thirouin@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 */
class ManagedConnection implements Connection, ExceptionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedConnection.class);

    private final List<ManagedSession> sessions = new ArrayList<ManagedSession>();
    private final AtomicBoolean needToStart = new AtomicBoolean(false);

    private final ConnectionDefinition connectionDefinition;
    private final JmsFactoryImpl jmsFactoryImpl;
    private final AtomicBoolean scheduleInProgress;
    private final ReentrantReadWriteLock connectionLock = new ReentrantReadWriteLock();

    private Connection connection;
    private ExceptionListener exceptionListener;

    ManagedConnection(ConnectionDefinition connectionDefinition, JmsFactoryImpl jmsFactoryImpl) throws JMSException {
        SeedCheckUtils.checkIfNotNull(connectionDefinition);

        this.jmsFactoryImpl = jmsFactoryImpl;
        this.connectionDefinition = connectionDefinition;
        this.scheduleInProgress = new AtomicBoolean(false);
        this.connection = createConnection();
    }

    private Connection createConnection() throws JMSException {
        Connection newConnection = jmsFactoryImpl.createRawConnection(connectionDefinition);

        // Set the exception listener to ourselves so we can monitor the underlying connection
        if (!connectionDefinition.isJeeMode()) {
            newConnection.setExceptionListener(this);
        }

        LOGGER.debug("Initialized JMS connection {}", connectionDefinition.getName());

        return newConnection;
    }

    private void scheduleReconnection() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                connectionLock.writeLock().lock();

                try {
                    // Recreate the connection
                    connection = createConnection();
                    LOGGER.info("Recreated JMS connection {}", connectionDefinition.getName());

                    // Refresh sessions
                    for (ManagedSession session : sessions) {
                        session.refresh(connection);
                    }

                    // Start the new connection if needed
                    if (needToStart.get()) {
                        connection.start();
                        scheduleInProgress.set(false);
                        LOGGER.info("Restarted JMS connection {}", connectionDefinition.getName());
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to restart JMS connection {}, next attempt in {} ms", connectionDefinition.getName(), connectionDefinition.getReconnectionDelay());
                    scheduleReconnection();
                } finally {
                    connectionLock.writeLock().unlock();
                }

            }
        };

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, connectionDefinition.getReconnectionDelay());
        new Timer().schedule(timerTask, calendar.getTime());
    }

    private Connection getConnection() throws JMSException {
        connectionLock.readLock().lock();
        try {
            if (connection == null) {
                throw new JMSException("Connection " + connectionDefinition.getName() + " is not ready");
            }

            return connection;
        } finally {
            connectionLock.readLock().unlock();
        }
    }

    @Override
    public void onException(JMSException exception) {
        LOGGER.error("An exception occurred on JMS connection {}", connectionDefinition.getName());
        if (exception != null && LOGGER.isDebugEnabled()) {
            LOGGER.debug("Original exception below", exception);
        }

        if (exceptionListener != null) {
            exceptionListener.onException(exception);
        }

        reset();
    }

    private void reset() {
        if (scheduleInProgress.getAndSet(true)) {
            LOGGER.debug("JMS connection {} already scheduled for restart", connectionDefinition.getName());
        } else {
            // reset the connection
            LOGGER.warn("Closing JMS connection {} and scheduling restart in {} ms", connectionDefinition.getName(), connectionDefinition.getReconnectionDelay());

            connectionLock.writeLock().lock();
            try {
                // Reset the sessions to prevent their use during refresh
                for (ManagedSession session : sessions) {
                    session.reset();
                }

                // Effectively close th connection and prevent its use during refresh
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (JMSException e) {
                        LOGGER.warn("Unable to cleanly close the JMS connection {}", connectionDefinition.getName());
                    }
                }
                connection = null;

                // Schedule
                scheduleReconnection();
            } finally {
                connectionLock.writeLock().unlock();
            }
        }
    }

    // Delegated methods

    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        connectionLock.readLock().lock();
        try {
            ManagedSession managedSession = new ManagedSession(getConnection().createSession(transacted, acknowledgeMode), transacted, acknowledgeMode, connectionDefinition.isJeeMode());
            sessions.add(managedSession);
            return managedSession;
        } finally {
            connectionLock.readLock().unlock();
        }
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Destination destination, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        return getConnection().createConnectionConsumer(destination, messageSelector, sessionPool, maxMessages);
    }

    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        return getConnection().createDurableConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool, maxMessages);
    }

    @Override
    public void close() throws JMSException {
        getConnection().close();
        LOGGER.info("Closed JMS connection {}", connectionDefinition.getName());
    }

    @Override
    public void start() throws JMSException {
        getConnection().start();
        needToStart.set(true);
        LOGGER.info("Started JMS connection {}", connectionDefinition.getName());
    }

    @Override
    public void stop() throws JMSException {
        getConnection().stop();
        needToStart.set(false);
        LOGGER.info("Stopped JMS connection {}", connectionDefinition.getName());
    }

    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        return getConnection().getMetaData();
    }

    @Override
    public void setExceptionListener(ExceptionListener exceptionListener) throws JMSException {
        this.exceptionListener = exceptionListener;
    }

    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        return exceptionListener;
    }

    @Override
    public void setClientID(String clientID) throws JMSException {
        throw new IllegalStateException("Client id cannot be changed on managed connections");
    }

    @Override
    public String getClientID() throws JMSException {
        throw new IllegalStateException("Client id cannot be retrieved on managed connections");
    }
}
