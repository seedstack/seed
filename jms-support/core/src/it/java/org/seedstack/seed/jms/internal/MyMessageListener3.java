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

import org.seedstack.seed.core.api.Logging;
import org.seedstack.seed.jms.api.DestinationType;
import org.seedstack.seed.jms.api.JmsMessageListener;
import org.slf4j.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * @author Pierre Thirouin <pierre.thirouin@ext.mpsa.com>
 *         07/11/2014
 */
@JmsMessageListener(connection = "connection3", destinationType = DestinationType.QUEUE, destinationName = "queue3")
public class MyMessageListener3 implements MessageListener {

    @Logging
    Logger logger;

    @Override
    public void onMessage(Message message) {
        try {
            ManagedReconnectionFeatureIT.text = ((TextMessage) message).getText();
            logger.info("Message '{}' received", ((TextMessage) message).getText());
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
        if (ManagedReconnectionFeatureIT.text.equals("MANAGED1")) {
            ManagedReconnectionFeatureIT.latchConnect1.countDown();
        } else if (ManagedReconnectionFeatureIT.text.equals("RECONNECT1")) {
            ManagedReconnectionFeatureIT.latchReconnect1.countDown();
        } else if (ManagedReconnectionFeatureIT.text.equals("RECONNECT2")) {
            ManagedReconnectionFeatureIT.latchReconnect2.countDown();
        }
    }
}
