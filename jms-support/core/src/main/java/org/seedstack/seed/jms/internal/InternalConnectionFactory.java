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

import org.seedstack.seed.jms.spi.ConnectionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Factory to create a jms connection (managed or not).
 *
 * @author Pierre Thirouin <pierre.thirouin@ext.mpsa.com>
 */
class InternalConnectionFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalConnectionFactory.class);
    private final AtomicLong unnamedConnectionSequence = new AtomicLong();
    private final String applicationName;

    InternalConnectionFactory(String applicationName) {
        this.applicationName = applicationName;
    }

    Connection create(ConnectionFactory connectionFactory, ConnectionDefinition connectionDefinition, String explicitConnectionName) throws JMSException {
        String connectionName;
        if (explicitConnectionName == null || "".equals(explicitConnectionName)) {
            connectionName = "unnamed-connection-" + unnamedConnectionSequence.getAndIncrement();
        } else {
            connectionName = explicitConnectionName;
        }

        String defaultClientId = applicationName + "-" + connectionName;

        Connection connection;
        if (connectionDefinition.isManaged()) {
            connection = new ManagedConnection(connectionFactory, connectionDefinition, defaultClientId, connectionName);

            // client id is set in the managed connection
        } else {
            if (connectionDefinition.getUser() != null) {
                connection = connectionFactory.createConnection(connectionDefinition.getUser(), connectionDefinition.getPassword());
            } else {
                connection = connectionFactory.createConnection();
            }

            // client id is set here on raw connection
            if (connectionDefinition.isShouldSetClientId()) {
                if (connectionDefinition.getClientId() == null) {
                    LOGGER.info("Setting client id as {} on unmanaged connection {}", defaultClientId, connectionName);
                    connection.setClientID(defaultClientId);
                } else {
                    LOGGER.info("Setting client id as {} on unmanaged connection {}", connectionDefinition.getClientId(), connectionName);
                    connection.setClientID(connectionDefinition.getClientId());
                }
            }
        }

        return connection;
    }
}
