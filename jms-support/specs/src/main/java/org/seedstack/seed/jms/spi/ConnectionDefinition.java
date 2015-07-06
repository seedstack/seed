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

import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;

public class ConnectionDefinition {
    private final String name;
    private final ConnectionFactory connectionFactory;

    private final boolean managed;
    private final boolean jeeMode;
    private final boolean shouldSetClientId;

    private final String clientId;
    private final String user;
    private final String password;
    private final int reconnectionDelay;

    private final Class<? extends ExceptionListener> exceptionListenerClass;
    private final Class<? extends JmsExceptionHandler> jmsExceptionHandlerClass;

    public ConnectionDefinition(String name, ConnectionFactory connectionFactory, boolean managed, boolean jeeMode, boolean shouldSetClientId, String clientId, String user, String password, int reconnectionDelay, Class<? extends ExceptionListener> exceptionListenerClass, Class<? extends JmsExceptionHandler> jmsExceptionHandlerClass) {
        this.name = name;
        this.connectionFactory = connectionFactory;

        this.managed = managed;
        this.jeeMode = jeeMode;
        this.shouldSetClientId = shouldSetClientId;

        this.clientId = clientId;
        this.user = user;
        this.password = password;
        this.reconnectionDelay = reconnectionDelay;

        this.exceptionListenerClass = exceptionListenerClass;
        this.jmsExceptionHandlerClass = jmsExceptionHandlerClass;
    }

    public String getName() {
        return name;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public Class<? extends ExceptionListener> getExceptionListenerClass() {
        return exceptionListenerClass;
    }

    public Class<? extends JmsExceptionHandler> getJmsExceptionHandlerClass() {
        return jmsExceptionHandlerClass;
    }

    public boolean isManaged() {
        return managed;
    }

    public int getReconnectionDelay() {
        return reconnectionDelay;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean isShouldSetClientId() {
        return shouldSetClientId;
    }

    public boolean isJeeMode() {
        return jeeMode;
    }
}
