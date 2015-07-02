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

import net.jcip.annotations.NotThreadSafe;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.seedstack.seed.core.api.Logging;
import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.seed.jms.fixtures.TestExceptionListener;
import org.seedstack.seed.jms.fixtures.TestSender3;
import org.seedstack.seed.jms.fixtures.TestSender4;
import org.seedstack.seed.jms.internal.FakeConnectionFactoryImpl;
import org.seedstack.seed.jms.spi.ConnectionDefinition;
import org.seedstack.seed.jms.spi.JmsFactory;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.Connection;
import javax.jms.JMSException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.fest.reflect.core.Reflection.method;
import static org.junit.Assert.fail;

/**
 * @author pierre.thirouin@ext.mpsa.com
 */
@RunWith(SeedITRunner.class)
@NotThreadSafe
public class JmsRefreshIT {

    // TODO <pith> 19/11/2014: improve these tests.

    @Logging
    Logger logger;

    @Inject
    TestSender3 testSender3;

    @Inject
    TestSender4 testSender4;

    public static CountDownLatch latchConnect1 = new CountDownLatch(1);
    public static CountDownLatch latchReconnect1 = new CountDownLatch(1);

    public static CountDownLatch latchReconnect2 = new CountDownLatch(1);

    public static String text = null;

    @Inject
    @Named("connection3")
    private Connection connection3;

    @Test
    public void reset_then_refresh_connection_should_works() throws Exception {
        // Send succeed
        testSender3.send("MANAGED1");
        latchConnect1.await(1, TimeUnit.SECONDS);
        Assertions.assertThat(text).isEqualTo("MANAGED1");

        // Reset connection

        connection3.close();
        method("onException").withParameterTypes(JMSException.class).in(connection3).invoke(new JMSException("Connection is down"));
        //Thread.sleep(200); // reconnect at the first try

        // Refresh connection and resend message
        boolean sent = false;
        int attempt = 0;
        while (!sent && attempt < 50) {
            try {
                attempt++;
                testSender3.send("RECONNECTED1");
                sent = true;
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }

        latchReconnect1.await(200, TimeUnit.MILLISECONDS);
        Assertions.assertThat(text).isEqualTo("RECONNECTED1"); // message is successfully received
    }



    @Test
    public void connection_failed_multiple_times_then_reconnect() throws InterruptedException, JMSException {
        JmsFactory connectionFactory = Whitebox.getInternalState(connection3, "jmsFactoryImpl");
        Whitebox.setInternalState(connection3, "jmsFactoryImpl", new FakeConnectionFactoryImpl());

        connection3.close();
        ((javax.jms.ExceptionListener) connection3).onException(new JMSException("Connection Closed"));

        String message = "MANAGED2";
        for (int i = 0; i < 4; i++) {
            try {
                logger.info("Send message: {}", message);
                testSender3.send(message);
                fail();
            } catch (Exception e) {
                Thread.sleep(10);
                logger.info("Unable to send message: {}", message);
            }
        }

        Whitebox.setInternalState(connection3, "jmsFactoryImpl", connectionFactory);
        Thread.sleep(200); // wait reconnection
        message = "RECONNECT2";
        testSender3.send(message);
        logger.info("Send message: {}", message);
        latchReconnect2.await(100, TimeUnit.MILLISECONDS);
        Assertions.assertThat(text).isEqualTo(message);
    }

    @Test
    public void test_that_wraped_exceptionlistener_from_managedConnection_is_declared_using_props() throws InterruptedException, JMSException {
        Class<? extends javax.jms.ExceptionListener> exceptionListener = ((ConnectionDefinition) Whitebox.getInternalState(connection3, "connectionDefinition")).getExceptionListenerClass();
        Assertions.assertThat(exceptionListener).isAssignableFrom(TestExceptionListener.class);
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
        testSender3.send("MANAGED1");
        latchConnect1.await(1, TimeUnit.SECONDS);
        Assertions.assertThat(text).isEqualTo("MANAGED1");

        logger.info("You should shutdown the broker...");
        Thread.sleep(10000);
        try {
            testSender3.send("SHOULD FAIL");
            fail();
        } catch (JMSException e) {
            logger.info("Failed to send message");

            logger.info("You should restart the broker...");
            Thread.sleep(10000);
            testSender3.send("RECONNECTED1");
            latchReconnect1.await(1, TimeUnit.SECONDS);
            Assertions.assertThat(text).isEqualTo("RECONNECTED1");
        }
    }

    //@Test
    public void manual_cyclic_fail() throws InterruptedException {
        logger.info("You should shutdown the broker...");
        Thread.sleep(5000);
        String message = "MANAGED1";
        while (true) {
            try {
                testSender3.send(message);
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
