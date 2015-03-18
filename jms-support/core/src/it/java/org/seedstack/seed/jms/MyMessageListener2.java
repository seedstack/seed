/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.jms;

import org.seedstack.seed.core.api.Logging;
import org.seedstack.seed.jms.api.DestinationType;
import org.seedstack.seed.jms.api.JmsMessageListener;
import org.slf4j.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;


@JmsMessageListener(connection = "connection2", destinationType = DestinationType.QUEUE, destinationName = "queue2")
public class MyMessageListener2 implements MessageListener {

    @Logging
    Logger logger;

    @Override
    public void onMessage(Message message) {
        try {
            SeedJMSPluginIT.textUnManaged = ((TextMessage) message).getText();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        } finally {
            SeedJMSPluginIT.unmanaged.countDown();
        }
    }
}