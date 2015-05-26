/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.jms.fixtures;

import org.seedstack.seed.it.api.ITBind;
import org.seedstack.seed.jms.api.JmsConnection;
import org.seedstack.seed.transaction.api.Transactional;

import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

@ITBind
public class TestSender1 {
    @Inject
    private Session session;

    @Transactional
    @JmsConnection("connection1")
    public void send(String stringMessage) throws JMSException {
        // Queue
        Destination queue = session.createQueue("queue1");
        //create Message
        TextMessage message1 = session.createTextMessage();
        message1.setText(stringMessage);

        //get Message Producer
        MessageProducer producer = session.createProducer(queue);
        //send Message
        producer.send(message1);
    }
}

