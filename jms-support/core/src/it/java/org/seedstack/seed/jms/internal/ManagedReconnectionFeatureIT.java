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

import net.jcip.annotations.NotThreadSafe;
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
@NotThreadSafe
public class ManagedReconnectionFeatureIT {

    // TODO <pith> 19/11/2014: improve these tests.

    @Logging
    Logger logger;

    @Inject
    MyManagedMessageSender3 myManagedMessageSender3;
    
    @Inject
    MyManagedMessageSender5 myManagedMessageSender5;

    static CountDownLatch latchConnect1 = new CountDownLatch(1);
    static CountDownLatch latchReconnect1 = new CountDownLatch(1);

    static CountDownLatch latchReconnect2 = new CountDownLatch(1);

    static String text = null;

    @Inject
    @Named("connection3")
    private Connection connection3;
    
    @Inject
    @Named("connection5")
    private Connection connection5;
    
    @Inject
    @Named("connection6")
    private Connection connection6;

    @Test
    public void reset_then_refresh_connection_should_works() throws Exception {
        // Send succeed
        myManagedMessageSender3.send("MANAGED1");
        latchConnect1.await(1, TimeUnit.SECONDS);
        Assertions.assertThat(text).isEqualTo("MANAGED1");

        // Reset connection
        connection3.close();
        ((ManagedConnection) connection3).onException(new JMSException("Connection is down"));
        Thread.sleep(200); // reconnect at the first try

        // Refresh connection and resend message
        myManagedMessageSender3.send("RECONNECTED1");

        latchReconnect1.await(200, TimeUnit.MILLISECONDS);
        Assertions.assertThat(text).isEqualTo("RECONNECTED1"); // message is successfully received
    }

    @Test
    public void connection_failed_multiple_times_then_reconnect() throws InterruptedException, JMSException {

        ConnectionFactory connectionFactory = Whitebox.getInternalState(connection3, "connectionFactory");
        Whitebox.setInternalState(connection3, "connectionFactory", new FakeConnectionFactory());

        connection3.close();
        ((ExceptionListener)connection3).onException(new JMSException("Connection Closed"));

        String message = "MANAGED2";
        for (int i = 0; i < 4; i++) {
            try {
                logger.info("Send message: {}", message);
                myManagedMessageSender3.send(message);
                fail();
            } catch (Exception e) {
                Thread.sleep(10);
                logger.info("Unable to send message: {}", message);
            }
        }

        Whitebox.setInternalState(connection3, "connectionFactory", connectionFactory);
        Thread.sleep(200); // wait reconnection
        message = "RECONNECT2";
        myManagedMessageSender3.send(message);
        logger.info("Send message: {}", message);
        latchReconnect2.await(100, TimeUnit.MILLISECONDS);
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
        myManagedMessageSender3.send("MANAGED1");
        latchConnect1.await(1, TimeUnit.SECONDS);
        Assertions.assertThat(text).isEqualTo("MANAGED1");

        logger.info("You should shutdown the broker...");
        Thread.sleep(10000);
        try {
            myManagedMessageSender3.send("SHOULD FAIL");
            fail();
        } catch (JMSException e) {
            logger.info("Failed to send message");

            logger.info("You should restart the broker...");
            Thread.sleep(10000);
            myManagedMessageSender3.send("RECONNECTED1");
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
                myManagedMessageSender3.send(message);
                break;
            } catch (JMSException e) {
                Thread.sleep(100);
                logger.info("Unable to send message: {}", message);
            }
        }
        latchConnect1.await(10, TimeUnit.SECONDS);
        Assertions.assertThat(text).isEqualTo(message);
    }
    
    
    @Test
    public void test_that_wraped_exceptionlistener_from_managedConnection_differs_from_jmsbroker_connection() throws InterruptedException, JMSException {
        Whitebox.setInternalState(connection5, "connectionFactory", new FakeConnectionFactory());
        ExceptionListener exceptionListener = new ExceptionListener() {        	
        	@Override
        	public void onException(JMSException e) {
        		logger.info("test");
        	}
        };
        ManagedConnection managedConnection = ((ManagedConnection)connection5);
        managedConnection.setExceptionListener(exceptionListener);
        Connection jmsConnection = Whitebox.getInternalState(connection5, "connection");
        ExceptionListener exceptionListenerAQ = Whitebox.getInternalState(jmsConnection, "exceptionListener");
        ExceptionListener exceptionListenerMC = Whitebox.getInternalState(managedConnection, "exceptionListener");
        Assertions.assertThat(exceptionListenerAQ).isNotEqualTo(exceptionListenerMC);
        Assertions.assertThat(exceptionListenerMC).isEqualTo(exceptionListener);       
        
    }
    
    @Test
    public void test_that_wraped_exceptionlistener_from_managedConnection_is_declared_using_props() throws InterruptedException, JMSException {
        ManagedConnection managedConnection = ((ManagedConnection)connection6);
        managedConnection.onException(new JMSException("test"));
        ExceptionListener exceptionListenerMC = Whitebox.getInternalState(managedConnection, "exceptionListener");
        Assertions.assertThat(exceptionListenerMC).isInstanceOf(MyExceptionListener.class);
        Logger logger = Whitebox.getInternalState(exceptionListenerMC, "LOGGER");
        Assertions.assertThat(logger).isNotNull();

        
    }

}
