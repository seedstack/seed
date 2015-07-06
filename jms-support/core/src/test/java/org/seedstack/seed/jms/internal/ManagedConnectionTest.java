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

import com.google.common.collect.Lists;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.seedstack.seed.jms.spi.ConnectionDefinition;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;


/**
 * @author pierre.thirouin@ext.mpsa.com
 *         12/11/2014
 */
@RunWith(MockitoJUnitRunner.class)
public class ManagedConnectionTest {

    private ManagedConnection underTest;
    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private Connection connection;
    @Mock
    private JmsFactoryImpl jmsFactoryImpl;
    @Mock
    private ExceptionListener exceptionListener;

    @Before
    public void setUp() throws JMSException {
        ConnectionDefinition connectionDefinition = new ConnectionDefinition("my-connection", connectionFactory, true, true, true, "test-client-id", "user", "password", 100, null, null);
        Mockito.when(connectionFactory.createConnection("user", "password")).thenReturn(connection);
        Mockito.when(connection.createSession(true, Session.AUTO_ACKNOWLEDGE)).thenReturn(Mockito.mock(Session.class));
        Mockito.when(connection.getClientID()).thenReturn("my-app-my-connection");
        Mockito.when(connection.getExceptionListener()).thenReturn(exceptionListener);
        Mockito.when(jmsFactoryImpl.createRawConnection(connectionDefinition)).thenReturn(connection);
        underTest = new ManagedConnection(connectionDefinition, jmsFactoryImpl);
    }

    @Test
    public void connection_should_be_refreshed_on_failure() throws JMSException, InterruptedException {
        // Initialization
        underTest.start();
        connection = Whitebox.getInternalState(underTest, "connection");
        Assertions.assertThat(connection).isNotNull();
        // Mock the created session
        ManagedSession session = Mockito.mock(ManagedSession.class);
        Whitebox.setInternalState(underTest, "sessions", Lists.newArrayList(session));

        // Failure
        Object jmsFactoryImpl = Whitebox.getInternalState(underTest, "jmsFactoryImpl");
        Whitebox.setInternalState(underTest, "jmsFactoryImpl", new FakeConnectionFactoryImpl());
        underTest.setExceptionListener(new javax.jms.ExceptionListener() {
			
			@Override
			public void onException(JMSException exception) {
				
			}
		});
        underTest.onException(new JMSException("Connection closed"));

        // Reset 
        connection = Whitebox.getInternalState(underTest, "connection");
        Assertions.assertThat(connection).isNull();
        Mockito.verify(session, Mockito.times(1)).reset(); // session is reset on cascade

        // The connection is back
        Whitebox.setInternalState(underTest, "jmsFactoryImpl", jmsFactoryImpl);

        // wait for the timer to refresh the connection
        Thread.sleep(200);

        connection = Whitebox.getInternalState(underTest, "connection"); // connection is back
        Assertions.assertThat(connection).isNotNull();
        Mockito.verify(session, Mockito.times(1)).refresh(connection); // session is refreshed
    }

    @Test
    public void test_that_wraped_exceptionlistener_from_managedConnection_differs_from_jmsbroker_connection() throws InterruptedException, JMSException {
        Whitebox.setInternalState(underTest, "jmsFactoryImpl", new FakeConnectionFactoryImpl());
        javax.jms.ExceptionListener exceptionListener = new javax.jms.ExceptionListener() {
            @Override
            public void onException(JMSException e) {

            }
        };
        underTest.setExceptionListener(exceptionListener);
        Connection jmsConnection = Whitebox.getInternalState(underTest, "connection");
        javax.jms.ExceptionListener exceptionListenerAQ = jmsConnection.getExceptionListener();
        javax.jms.ExceptionListener exceptionListenerMC = Whitebox.getInternalState(underTest, "exceptionListener");
        Assertions.assertThat(exceptionListenerAQ).isNotEqualTo(exceptionListenerMC);
        Assertions.assertThat(exceptionListenerMC).isEqualTo(exceptionListener);

    }
}
