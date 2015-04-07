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

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import javax.jms.*;

import static org.mockito.Mockito.*;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         12/11/2014
 */
@RunWith(MockitoJUnitRunner.class)
public class ManagedMessageConsumerTest {

    private ManagedMessageConsumer underTest;
    @Mock
    private MessageConsumer messageConsumer;
    @Mock
    private Destination destination;
    @Mock
    private Session session;

    private MyMessageListener messageListener = new MyMessageListener();

    @Before
    public void setUp() throws JMSException {
        underTest = new ManagedMessageConsumer(messageConsumer, destination, null, false, false);
        when(session.createConsumer(destination)).thenReturn(messageConsumer);
    }

    @Test
    public void messageConsumer_is_reset_then_refreshed() throws JMSException {
        MessageConsumer actualMessageConsumer = Whitebox.getInternalState(underTest, "messageConsumer");
        Assertions.assertThat(actualMessageConsumer).isEqualTo(messageConsumer);
        underTest.setMessageListener(messageListener);

        // Reset message consumer
        underTest.reset();

        actualMessageConsumer = Whitebox.getInternalState(underTest, "messageConsumer");
        Assertions.assertThat(actualMessageConsumer).isNull();

        // Refresh message consumer and the message listeners on cascade
        underTest.refresh(session);
        actualMessageConsumer = Whitebox.getInternalState(underTest, "messageConsumer");
        Assertions.assertThat(actualMessageConsumer).isNotNull();

        // method call at the init and then at the refresh
        verify(messageConsumer, times(2)).setMessageListener(messageListener);
    }

    class MyMessageListener implements MessageListener {
        @Override
        public void onMessage(Message message) { }
    }
}
