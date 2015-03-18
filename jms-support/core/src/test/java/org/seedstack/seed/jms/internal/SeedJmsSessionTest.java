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

import org.seedstack.seed.transaction.utils.TransactionalProxy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.jms.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SeedJmsSessionTest {

    private Session underTest;

    private Session session;

    @Before
    public void setUp() {
        JmsSessionLink sessionLink = mock(JmsSessionLink.class);
        session = mock(Session.class);
        when(sessionLink.get()).thenReturn(session);
        underTest = TransactionalProxy.create(Session.class, sessionLink);
    }

    @Test
    public void testCreateBytesMessage() throws JMSException {
        when(session.createBytesMessage()).thenAnswer(new Answer<BytesMessage>() {
            @Override
            public BytesMessage answer(InvocationOnMock invocationOnMock) throws Throwable {

                return mock(BytesMessage.class);
            }
        });
        assertThat(underTest.createBytesMessage()).isNotNull();
    }

    @Test
    public void testCreateMapMessage() throws JMSException {
        when(session.createMapMessage()).thenAnswer(new Answer<MapMessage>() {
            @Override
            public MapMessage answer(InvocationOnMock invocationOnMock) throws Throwable {
                return mock(MapMessage.class);
            }
        });
        assertThat(underTest.createMapMessage()).isNotNull();
    }


    @Test
    public void testCreateMessage() throws JMSException {
        Message mockMessage = mock(Message.class);
        when(session.createMessage()).thenReturn(mockMessage);
        assertThat(underTest.createMessage()).isNotNull();
    }


    @Test
    public void testCreateObjectMessage() throws JMSException {
        ObjectMessage mockObjectMessage = mock(ObjectMessage.class);
        when(session.createObjectMessage()).thenReturn(mockObjectMessage);
        assertThat(underTest.createObjectMessage()).isNotNull();
    }

    @Test
    public void testCreateObjectMessage_with_serializer() throws JMSException {
        when(session.createObjectMessage(any(Serializable.class))).thenAnswer(new Answer<ObjectMessage>() {
            @Override
            public ObjectMessage answer(InvocationOnMock invocationOnMock) throws Throwable {
                ObjectMessage mockObjectMessage = mock(ObjectMessage.class);
                when(mockObjectMessage.getObject()).thenReturn((Serializable) invocationOnMock.getArguments()[0]);
                return mockObjectMessage;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        final ObjectMessage objectMessage = underTest.createObjectMessage(5);
        assertThat(objectMessage).isNotNull();
        assertThat(objectMessage.getObject()).isEqualTo(5);

    }

    @Test
    public void testCreateStreamMessage() throws JMSException {
        StreamMessage mockStreamMessage = mock(StreamMessage.class);
        when(session.createStreamMessage()).thenReturn(mockStreamMessage);
        assertThat(underTest.createStreamMessage()).isNotNull();
    }


    @Test
    public void testCreateTextMessage() throws JMSException {
        TextMessage mockTextMessage = mock(TextMessage.class);
        when(session.createTextMessage()).thenReturn(mockTextMessage);
        assertThat(underTest.createTextMessage()).isNotNull();
    }

    @Test
    public void testCreateTextMessage_with_text() throws JMSException {
        when(session.createTextMessage("toto")).thenAnswer(new Answer<TextMessage>() {
            @Override
            public TextMessage answer(InvocationOnMock invocationOnMock) throws Throwable {
                TextMessage mockTextMessage = mock(TextMessage.class);
                when(mockTextMessage.getText()).thenReturn((String) invocationOnMock.getArguments()[0]);
                return mockTextMessage;
            }
        });
        final TextMessage textMessage = underTest.createTextMessage("toto");
        assertThat(textMessage).isNotNull();
        assertThat(textMessage.getText()).isEqualTo("toto");

    }

    @Test
    public void testGetTransacted() throws JMSException {
        when(session.getTransacted()).thenReturn(false);
        final boolean transacted = underTest.getTransacted();
        assertThat(transacted).isFalse();
    }

    @Test
    public void testGetAcknowledgeMode() throws JMSException {
        when(session.getAcknowledgeMode()).thenReturn(javax.jms.Session.AUTO_ACKNOWLEDGE);
        assertThat(underTest.getAcknowledgeMode()).isEqualTo(javax.jms.Session.AUTO_ACKNOWLEDGE);
    }

    @Test
    public void testCommit() throws JMSException {
        final boolean[] isCommit = {false};
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                isCommit[0] = true;
                return null;
            }
        }).when(session).commit();
        underTest.commit();
        assertThat(isCommit[0]).isTrue();

    }

    @Test
    public void testRollback() throws JMSException {
        final boolean[] isRollback = {false};
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                isRollback[0] = true;
                return null;
            }
        }).when(session).rollback();

        underTest.rollback();
        assertThat(isRollback[0]).isTrue();

    }

    @Test
    public void testClose() throws JMSException {
        final boolean[] isClosed = {false};
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                isClosed[0] = true;
                return null;
            }
        }).when(session).close();
        underTest.close();
        assertThat(isClosed[0]).isTrue();
    }

    @Test
    public void testRecover() throws JMSException {
        final boolean[] isRecovered = {false};
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                isRecovered[0] = true;
                return null;
            }
        }).when(session).recover();

        underTest.recover();

        assertThat(isRecovered[0]).isTrue();

    }

    @Test
    public void testGetMessageListener() throws JMSException {
        MessageListener mockListener = mock(MessageListener.class);
        when(session.getMessageListener()).thenReturn(mockListener);
        assertThat(underTest.getMessageListener()).isEqualTo(mockListener);
    }

    @Test
    public void testSetMessageListener() throws JMSException {
        final MessageListener mockListener = mock(MessageListener.class);
        final List<MessageListener> listeners = new ArrayList<MessageListener>();
        doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                final MessageListener listener = (MessageListener) invocationOnMock.getArguments()[0];
                listeners.add(listener);
                return null;
            }
        }).when(session).setMessageListener(mockListener);

        underTest.setMessageListener(mockListener);
        assertThat(listeners).isNotEmpty();
        assertThat(listeners).hasSize(1);

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRun() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                throw new UnsupportedOperationException();
            }
        }).when(session).run();

        underTest.run();
    }

    @Test
    public void testCreateProducer() throws JMSException {
        Destination mockDestination = mock(Destination.class);
        MessageProducer mockMessageProducer = mock(MessageProducer.class);
        when(session.createProducer(mockDestination)).thenReturn(mockMessageProducer);
        final MessageProducer producer = underTest.createProducer(mockDestination);
        assertThat(producer).isNotNull();
        assertThat(producer).isEqualTo(mockMessageProducer);
    }


    @Test
    public void testCreateConsumer() throws JMSException {
        Destination mockDestination = mock(Destination.class);
        MessageConsumer mockConsumer = mock(MessageConsumer.class);
        when(session.createConsumer(mockDestination)).thenReturn(mockConsumer);
        final MessageConsumer consumer = underTest.createConsumer(mockDestination);
        assertThat(consumer).isEqualTo(mockConsumer);

    }


    @Test
    public void testCreateConsumer_with_selector() throws JMSException {
        Destination mockDestination = mock(Destination.class);
        when(session.createConsumer(any(Destination.class), anyString())).thenAnswer(new Answer<MessageConsumer>() {
            @Override
            public MessageConsumer answer(InvocationOnMock invocationOnMock) throws Throwable {
                MessageConsumer mockConsumer = mock(MessageConsumer.class);
                when(mockConsumer.getMessageSelector()).thenReturn((String) invocationOnMock.getArguments()[1]);
                return mockConsumer;
            }
        });
        final MessageConsumer consumer = underTest.createConsumer(mockDestination, "selector");
        assertThat(consumer).isNotNull();
        assertThat(consumer.getMessageSelector()).isEqualTo("selector");

    }

    @Test
    public void testCreateConsumer_with_selector_and_nolocal() throws JMSException {
        Destination mockDestination = mock(Destination.class);
        when(session.createConsumer(any(Destination.class), anyString(), anyBoolean())).thenAnswer(new Answer<MessageConsumer>() {
            @Override
            public MessageConsumer answer(InvocationOnMock invocationOnMock) throws Throwable {
                MessageConsumer mockConsumer = mock(MessageConsumer.class);
                when(mockConsumer.getMessageSelector()).thenReturn((String) invocationOnMock.getArguments()[1]);
                assertThat(invocationOnMock.getArguments()[2]).isInstanceOf(Boolean.class);
                return mockConsumer;
            }
        });
        final MessageConsumer consumer = underTest.createConsumer(mockDestination, "selector", false);
        assertThat(consumer).isNotNull();
        assertThat(consumer.getMessageSelector()).isEqualTo("selector");

    }

    @Test
    public void testCreateQueue() throws JMSException {

        String queueName = "myQueue";
        Queue mockQueue = mock(Queue.class);
        when(mockQueue.getQueueName()).thenReturn(queueName);
        when(session.createQueue(queueName)).thenReturn(mockQueue);
        final Queue myQueue = underTest.createQueue(queueName);
        assertThat(myQueue).isNotNull();
        assertThat(myQueue.getQueueName()).isEqualTo(queueName);
    }

    @Test
    public void testCreateTopic() throws JMSException {
        final String topicName = "topic1";
        Topic mockTopic = mock(Topic.class);
        when(mockTopic.getTopicName()).thenReturn(topicName);
        when(session.createTopic(topicName)).thenReturn(mockTopic);
        final Topic topic = underTest.createTopic(topicName);
        assertThat(topic).isNotNull();
        assertThat(topic).isEqualTo(mockTopic);
        assertThat(topic.getTopicName()).isEqualTo(topicName);
    }


    @Test
    public void testCreateDurableSubscriber() throws JMSException {
        Topic mockTopic = mock(Topic.class);
        TopicSubscriber mockSubscriber = mock(TopicSubscriber.class);
        when(session.createDurableSubscriber(mockTopic, "subscriber")).thenReturn(mockSubscriber);
        final TopicSubscriber subscriber = underTest.createDurableSubscriber(mockTopic, "subscriber");
        assertThat(subscriber).isNotNull();
        assertThat(subscriber).isEqualTo(mockSubscriber);
    }


    @Test
    public void testCreateBrowser() throws JMSException {
        Queue mockQueue = mock(Queue.class);
        QueueBrowser mockQueueBrowser = mock(QueueBrowser.class);
        when(session.createBrowser(mockQueue)).thenReturn(mockQueueBrowser);
        underTest.createBrowser(mockQueue);
    }

    @Test
    public void testCreateTemporaryQueue() throws JMSException {
        TemporaryQueue mockQueue = mock(TemporaryQueue.class);
        when(session.createTemporaryQueue()).thenReturn(mockQueue);
        final Queue myQueue = underTest.createTemporaryQueue();
        assertThat(myQueue).isNotNull();
        assertThat(myQueue).isEqualTo(mockQueue);
    }

    @Test
    public void testCreateTemporaryTopic() throws JMSException {
        TemporaryTopic mockTopic = mock(TemporaryTopic.class);
        when(session.createTemporaryTopic()).thenReturn(mockTopic);
        final TemporaryTopic topic = underTest.createTemporaryTopic();
        assertThat(topic).isNotNull();
        assertThat(topic).isEqualTo(mockTopic);
    }

    @Test
    public void testUnsubscribe() throws JMSException {

        final Map<String, Boolean> subscribeHashMap = new HashMap<String, Boolean>();
        subscribeHashMap.put("name", true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                final String s = (String) invocationOnMock.getArguments()[0];

                subscribeHashMap.put(s, false);
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        }).when(session).unsubscribe(anyString());
        underTest.unsubscribe("name");
        assertThat(subscribeHashMap.get("name")).isFalse();

    }
}
