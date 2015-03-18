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

import javax.jms.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This connection is a facade to the actual jms connection. It provides the reconnection mechanism.
 *
 * @author Pierre Thirouin <pierre.thirouin@ext.mpsa.com>
 *         04/11/2014
 */
class ManagedConnection implements Connection, ExceptionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedConnection.class);

    private final List<ManagedSession> sessions = new ArrayList<ManagedSession>();
    private final AtomicBoolean needToStart = new AtomicBoolean(false);
    private final String connectionName;
    private final int reconnectionDelay;
    private final ConnectionFactory connectionFactory;
    private final String user;
    private final String password;
    private final String defaultClientId;
    private final boolean shouldSetClientId;
    private String clientId;

    private ExceptionListener exceptionListener;
    private Connection connection;
    private ReentrantReadWriteLock connectionLock = new ReentrantReadWriteLock();

    ManagedConnection(ConnectionFactory connectionFactory, ConnectionDefinition connectionDefinition, String defaultClientId, String connectionName) throws JMSException {
        SeedCheckUtils.checkIfNotNull(connectionFactory);
        SeedCheckUtils.checkIfNotNull(connectionDefinition);

        this.connectionFactory = connectionFactory;
        this.connectionName = connectionName;
        this.reconnectionDelay = connectionDefinition.getReconnectionDelay();
        this.clientId = connectionDefinition.getClientId();
        this.user = connectionDefinition.getUser();
        this.password = connectionDefinition.getPassword();
        this.defaultClientId = defaultClientId;
        this.shouldSetClientId = connectionDefinition.isShouldSetClientId();

        this.connection = createConnection();
    }

    private Connection createConnection() throws JMSException {
        Connection newConnection;

        if (user != null) {
            newConnection = connectionFactory.createConnection(user, password);
        } else {
            newConnection = connectionFactory.createConnection();
        }

        // Initialize the client id
        try {
            if (shouldSetClientId) {
                if (clientId == null) {
                    LOGGER.info("Setting client id as {} on managed connection {}", defaultClientId, connectionName);
                    newConnection.setClientID(defaultClientId);
                } else {
                    LOGGER.info("Setting client id as {} on managed connection {}", clientId, connectionName);
                    newConnection.setClientID(clientId);
                }
            }
        } catch (JMSException e) {
            LOGGER.error(e.getErrorCode(), e);
            throw e;
        }

        newConnection.setExceptionListener(this);

        LOGGER.debug("Initialized JMS connection {}", connectionName);

        return newConnection;
    }

    private void refresh() {
        connectionLock.writeLock().lock();

        try {
            // Recreate the connection
            connection = createConnection();

            // Refresh sessions
            for (ManagedSession session : sessions) {
                session.refresh(connection);
            }

            // Start the new connection if needed
            if (needToStart.get()) {
                connection.start();
                LOGGER.info("Restarted JMS connection {}", connectionName);
            }
        } catch (JMSException e) {
            LOGGER.info("Attempt to reconnect {} in {} milliseconds", connectionName, reconnectionDelay);
            scheduleReconnection();
        } finally {
            connectionLock.writeLock().unlock();
        }
    }

    private void scheduleReconnection() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                refresh();
            }
        };
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, reconnectionDelay);
        new Timer().schedule(timerTask, calendar.getTime());
    }

    private Connection getConnection() throws JMSException {
        connectionLock.readLock().lock();
        try {
            if (connection == null) {
                throw new JMSException("Connection " + connectionName + " is not ready");
            }

            return connection;
        } finally {
            connectionLock.readLock().unlock();
        }
    }

    @Override
    public void onException(JMSException exception) {
        // call initial exception listener if any
        if (exceptionListener != null) {
            exceptionListener.onException(exception);
        }

        // reset the connection
        LOGGER.info("Reset JMS connection {}", connectionName);

        connectionLock.writeLock().lock();
        try {
            connection = null;
            for (ManagedSession session : sessions) {
                session.reset();
            }

            refresh();
        } finally {
            connectionLock.writeLock().unlock();
        }
    }

    // Delegated methods

    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        connectionLock.readLock().lock();
        try {
            ManagedSession managedSession = new ManagedSession(getConnection().createSession(transacted, acknowledgeMode), transacted, acknowledgeMode);
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
    public void setClientID(String clientID) throws JMSException {
        getConnection().setClientID(clientID);
        this.clientId = clientID;
    }

    @Override
    public String getClientID() throws JMSException {
        return getConnection().getClientID();
    }

    @Override
    public void close() throws JMSException {
        getConnection().close();
        LOGGER.info("Closed JMS connection {}", connectionName);
    }

    @Override
    public void start() throws JMSException {
        getConnection().start();
        needToStart.set(true);
        LOGGER.info("Started JMS connection {}", connectionName);
    }

    @Override
    public void stop() throws JMSException {
        getConnection().stop();
        needToStart.set(false);
        LOGGER.info("Stopped JMS connection {}", connectionName);
    }

    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        return getConnection().getMetaData();
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) throws JMSException {
        getConnection().setExceptionListener(listener);
        this.exceptionListener = listener;
    }

    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        return getConnection().getExceptionListener();
    }
}
