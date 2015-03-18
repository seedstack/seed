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


import org.seedstack.seed.transaction.spi.TransactionMetadata;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

class JmsTransactionHandler extends AbstractJmsTransactionHandler {
    private Connection connection;
    private JmsSessionLink jmsSessionLink;

    JmsTransactionHandler(JmsSessionLink jmsSessionLink, Connection connection) {
        this.jmsSessionLink = jmsSessionLink;
        this.connection = connection;
    }

    @Override
    public void doInitialize(TransactionMetadata transactionMetadata) {
        try {
            Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            jmsSessionLink.push(session);
        } catch (JMSException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Session doCreateTransaction() {
        return jmsSessionLink.get();
    }

    @Override
    public void doCleanup() {
        try {
            jmsSessionLink.pop().close();
        } catch (JMSException e) {
            throw new IllegalStateException("Unable to release JMS transaction", e);
        }
    }

    @Override
    public Session getCurrentTransaction() {
        return jmsSessionLink.getCurrentTransaction();
    }
}
