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

import org.seedstack.seed.it.api.ITBind;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.*;

/**
 * @author Pierre Thirouin <pierre.thirouin@ext.mpsa.com>
 *         18/11/2014
 */
@ITBind
public class MyUnManagedMessageSender3 implements MySender {

    @Inject
    @Named("connection4")
    private Connection connection;

    public void send(String stringMessage) throws JMSException {
        //create connection
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // create destination
        Destination queue = session.createQueue("queue4");
        //create Message
        TextMessage message1 = session.createTextMessage();
        message1.setText(stringMessage);
        //add Message Properties
        message1.setJMSExpiration(1000);
        message1.setJMSReplyTo(queue);
        //get Message Producer
        MessageProducer producer = session.createProducer(queue);
        //send Message
        producer.send(message1);
        session.close();
    }
}
