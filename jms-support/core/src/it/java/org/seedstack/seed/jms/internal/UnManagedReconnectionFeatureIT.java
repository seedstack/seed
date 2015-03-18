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
import org.seedstack.seed.it.SeedITRunner;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

/**
 * @author Pierre Thirouin <pierre.thirouin@ext.mpsa.com>
 *         07/11/2014
 */
@RunWith(SeedITRunner.class)
public class UnManagedReconnectionFeatureIT {

    // TODO <pith> 19/11/2014: improve these tests.

    @Logging
    Logger logger;

    @Inject
    MyUnManagedMessageSender3 myUnManagedMessageSender3;

    static CountDownLatch latchConnect1 = new CountDownLatch(1);
    static CountDownLatch latchReconnect1 = new CountDownLatch(1);

    static CountDownLatch latchConnect2 = new CountDownLatch(1);
    static CountDownLatch latchReconnect2 = new CountDownLatch(1);

    static String text = null;

    @Inject
    @Named("connection4")
    private Connection connection4;


    @Test
    public void reset_then_refresh_connection_should_works() throws Exception {
        // Send succeed
        myUnManagedMessageSender3.send("UNMANAGED1");
        latchConnect1.await(1, TimeUnit.SECONDS);
        Assertions.assertThat(text).isEqualTo("UNMANAGED1");

        // Reset connection
        connection4.close();
        ((ManagedConnection) connection4).onException(new JMSException("Connection is down"));
        Thread.sleep(200); // reconnect at the first try

        // Refresh connection and resend message
        myUnManagedMessageSender3.send("RECONNECTED1");
        latchReconnect1.await(200, TimeUnit.MILLISECONDS);
        Assertions.assertThat(text).isEqualTo("RECONNECTED1"); // message is successfully received
    }

    @Test
    public void connection_failed_multiple_times_then_reconnect() throws InterruptedException, JMSException {

        ConnectionFactory connectionFactory = Whitebox.getInternalState(connection4, "connectionFactory");
        Whitebox.setInternalState(connection4, "connectionFactory", new FakeConnectionFactory());

        connection4.close();
        ((ExceptionListener)connection4).onException(new JMSException("Connection Closed"));

        String message = "UNMANAGED2";
        for (int i = 0; i < 4; i++) {
            try {
                logger.info("Send message: {}", message);
                myUnManagedMessageSender3.send(message);
                fail();
            } catch (Exception e) {
                Thread.sleep(100);
                logger.info("Unable to send message: {}", message);
            }
        }

        Whitebox.setInternalState(connection4, "connectionFactory", connectionFactory);
        Thread.sleep(200); // wait reconnection
        message = "RECONNECT2";
        myUnManagedMessageSender3.send(message);
        logger.info("Send message: {}", message);
        latchReconnect2.await(10, TimeUnit.SECONDS);
        Assertions.assertThat(text).isEqualTo(message);
    }




    //                  MANUAL TESTS
    //
    // To test the reconnection feature with an actual broker.
    //
    // /!\ Don't forget to change the broker url in the props file.
    //

    //@Test
    public void manual_test() throws Exception {
        // Send succeed
        myUnManagedMessageSender3.send("UNMANAGED1");
        latchConnect1.await(1, TimeUnit.SECONDS);
        Assertions.assertThat(text).isEqualTo("UNMANAGED1");

        logger.info("You should shutdown the broker...");
        Thread.sleep(10000);
        try {
            myUnManagedMessageSender3.send("SHOULD FAIL");
            fail();
        } catch (JMSException e) {
            logger.info("Failed to send message");

            logger.info("You should restart the broker...");
            Thread.sleep(10000);
            myUnManagedMessageSender3.send("RECONNECTED1");
            latchReconnect1.await(1, TimeUnit.SECONDS);
            Assertions.assertThat(text).isEqualTo("RECONNECTED1");
        }
    }

    //@Test
    public void manual_cyclic_fail() throws InterruptedException {
        logger.info("You should shutdown the broker...");
        Thread.sleep(5000);
        String message = "UNMANAGED1";
        while (true) {
            try {
                myUnManagedMessageSender3.send(message);
                break;
            } catch (JMSException e) {
                Thread.sleep(100);
                logger.info("Unable to send message: {}", message);
            }
        }
        latchConnect1.await(10, TimeUnit.SECONDS);
        Assertions.assertThat(text).isEqualTo(message);
    }

}
