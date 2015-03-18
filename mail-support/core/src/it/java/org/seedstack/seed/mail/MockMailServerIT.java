/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.mail;

import com.google.common.base.Predicate;
import org.seedstack.seed.it.AbstractSeedIT;
import org.seedstack.seed.mail.api.MessageRetriever;
import org.seedstack.seed.mail.api.WithMailServer;
import org.junit.Test;
import org.seedstack.seed.mail.assertions.MockMailServerAssertions;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import static org.seedstack.seed.mail.assertions.MockMailServerAssertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@WithMailServer(host = "localhost", port = 6457)
public class MockMailServerIT extends AbstractSeedIT {
    @Inject
    MessageRetriever retriever;
    @Inject
    @Named("smtp-test")
    private Session session;

    private MimeMessage createMessage() throws MessagingException {
        MimeMessage message = new MimeMessage(session);

        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("test@test.com"));
        message.setFrom(new InternetAddress("test@test.com"));
        message.setSubject("Test Subject");
        message.setText("Test Body Text");
        message.setSentDate(new Date());
        message.setReplyTo(InternetAddress.parse("test@test.com"));

        return message;
    }

    @Test
    public void test_sent_message_has_the_same_recipients_message_from_server() throws MessagingException {
        final Message message = createMessage();
        Transport transport = session.getTransport();
        transport.connect(TestConstantsValues.DEFAULT_USERNAME, TestConstantsValues.DEFAULT_PASSWORD);
        transport.sendMessage(message, message.getAllRecipients());

        final Collection<Message> messages = retriever.getSentMessages();
        for (Message message1 : messages) {
            MockMailServerAssertions.assertThat(message1).hasRecipients(Message.RecipientType.TO);
            MockMailServerAssertions.assertThat(message1).recipientEqualsTo(Message.RecipientType.TO, InternetAddress.parse("test@test.com"));

        }

    }

    @Test(expected = Throwable.class)
    public void test_sent_message_are_not_same_recipients_message_from_server() throws MessagingException {
        final Message message = createMessage();
        Transport transport = session.getTransport();
        transport.connect(TestConstantsValues.DEFAULT_USERNAME, TestConstantsValues.DEFAULT_PASSWORD);
        transport.sendMessage(message, message.getAllRecipients());
        MockMailServerAssertions.assertThat(message).hasRecipients(Message.RecipientType.TO);
        MockMailServerAssertions.assertThat(message).recipientEqualsTo(Message.RecipientType.TO, InternetAddress.parse("test222222@test.com"));
    }

    @Test
    public void test_sent_message_has_same_from() throws MessagingException {
        final Message message = createMessage();
        Transport transport = session.getTransport();
        transport.connect(TestConstantsValues.DEFAULT_USERNAME, TestConstantsValues.DEFAULT_PASSWORD);
        transport.sendMessage(message, message.getAllRecipients());
        MockMailServerAssertions.assertThat(message).checkByCondition(new Predicate<Message>() {
            @Override
            public boolean apply(@Nullable Message input) {
                try {
                    if (input != null) {
                        return Arrays.equals(input.getFrom(), message.getFrom());
                    }
                } catch (MessagingException e) {
                    fail("FROM section header not equal");
                }
                return false;
            }
        });
    }

    @Test
    public void test_mock_message() throws MessagingException {
        final Message message = createMessage();
        Transport transport = session.getTransport();
        transport.connect(TestConstantsValues.DEFAULT_USERNAME, TestConstantsValues.DEFAULT_PASSWORD);
        transport.sendMessage(message, message.getAllRecipients());
        MockMailServerAssertions.assertThat(message).hasSentDate();
    }
}