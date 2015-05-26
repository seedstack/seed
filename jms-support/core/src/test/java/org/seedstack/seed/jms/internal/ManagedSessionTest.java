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

import javax.jms.*;
import java.util.List;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         12/11/2014
 */
@RunWith(MockitoJUnitRunner.class)
public class ManagedSessionTest {

    private ManagedSession underTest;
    @Mock
    private Connection connection;
    @Mock
    private Session session;
    @Mock
    private Destination destination;

    @Before
    public void setUp() throws JMSException {
        underTest = new ManagedSession(session, true, Session.AUTO_ACKNOWLEDGE, false);
        Mockito.when(session.createConsumer(destination, null, false)).thenReturn(Mockito.mock(MessageConsumer.class));
    }

    @Test
    public void session_is_reset() throws JMSException {
        // Check the session state
        Session actualSession = Whitebox.getInternalState(underTest, "session");
        Assertions.assertThat(actualSession).isNotNull();

        // Create two consumers
        underTest.createConsumer(destination);
        underTest.createConsumer(destination);
        List<ManagedMessageConsumer> managedMessageConsumers = Whitebox.getInternalState(underTest, "managedMessageConsumers");
        Assertions.assertThat(managedMessageConsumers).hasSize(2);

        // Mock the message consumers
        ManagedMessageConsumer messageConsumer1 = Mockito.mock(ManagedMessageConsumer.class);
        ManagedMessageConsumer messageConsumer2 = Mockito.mock(ManagedMessageConsumer.class);
        Whitebox.setInternalState(underTest, "managedMessageConsumers", Lists.newArrayList(messageConsumer1, messageConsumer2));

        // reset the connection and the message consumers on cascade
        underTest.reset();
        actualSession = Whitebox.getInternalState(underTest, "session");
        Assertions.assertThat(actualSession).isNull();
        Mockito.verify(messageConsumer1, Mockito.times(1)).reset();
        Mockito.verify(messageConsumer2, Mockito.times(1)).reset();
    }

    @Test
    public void session_is_refreshed() throws JMSException {
        // Mock
        ManagedMessageConsumer messageConsumer1 = Mockito.mock(ManagedMessageConsumer.class);
        ManagedMessageConsumer messageConsumer2 = Mockito.mock(ManagedMessageConsumer.class);
        Whitebox.setInternalState(underTest, "managedMessageConsumers", Lists.newArrayList(messageConsumer1, messageConsumer2));
        Mockito.when(connection.createSession(true, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);

        // Create two consumers
        underTest.createConsumer(destination);
        underTest.createConsumer(destination);

        // reset session
        underTest.reset();

        // refresh session
        underTest.refresh(connection);
        Session actualSession = Whitebox.getInternalState(underTest, "session");
        Assertions.assertThat(actualSession).isNotNull();

        // refresh consumers on cascade
        Mockito.verify(messageConsumer1, Mockito.times(1)).refresh(actualSession);
        Mockito.verify(messageConsumer2, Mockito.times(1)).refresh(actualSession);
    }
}
