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
 * @author Pierre Thirouin <pierre.thirouin@ext.mpsa.com>
 *         12/11/2014
 */
@RunWith(MockitoJUnitRunner.class)
public class ManagedConnectionTest {

    private ManagedConnection underTest;
    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private Connection connection;

    @Before
    public void setUp() throws JMSException {
        Mockito.when(connectionFactory.createConnection("user", "password")).thenReturn(connection);
        Mockito.when(connection.createSession(true, Session.AUTO_ACKNOWLEDGE)).thenReturn(Mockito.mock(Session.class));
        Mockito.when(connection.getClientID()).thenReturn("my-app-my-connection");
        underTest = new ManagedConnection(connectionFactory, new ConnectionDefinition(true, true, null, "user", "password", 100), "test-client-id", "my-connection");
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
        Whitebox.setInternalState(underTest, "connectionFactory", new FakeConnectionFactory());
        underTest.setExceptionListener(new ExceptionListener() {
			
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
        Whitebox.setInternalState(underTest, "connectionFactory", connectionFactory);

        // wait for the timer to refresh the connection
        Thread.sleep(200);

        connection = Whitebox.getInternalState(underTest, "connection"); // connection is back
        Assertions.assertThat(connection).isNotNull();
        Mockito.verify(session, Mockito.times(1)).refresh(connection); // session is refreshed
    }

}
