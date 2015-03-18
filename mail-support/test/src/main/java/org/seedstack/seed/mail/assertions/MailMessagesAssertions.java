/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.mail.assertions;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import org.seedstack.seed.core.api.SeedException;
import org.assertj.core.api.AbstractAssert;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.seedstack.seed.mail.assertions.MailAssertionsErrorCodes.ERROR_OCCURED_WHILE_EXTRACTING_MESSAGE_FROM_SERVER;
import static org.seedstack.seed.mail.assertions.MailAssertionsErrorCodes.NOT_SAME_RECIPIENTS_SIZE;
import static org.seedstack.seed.mail.assertions.MailAssertionsErrorCodes.NO_MAIL_SUBJECT_SPECIFIED;
import static org.seedstack.seed.mail.assertions.MailAssertionsErrorCodes.NO_RECIPIENTS_SPECIFIED;
import static org.seedstack.seed.mail.assertions.MailAssertionsErrorCodes.NO_SENT_DATE_FOUND;
import static javax.mail.Message.RecipientType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Assertion class over messages received by the mock server in order to verify their integrity
 *
 * @param <S> the assertion class
 * @author aymen.benhmida@ext.mpsa.com
 */
public class MailMessagesAssertions<S extends Message> extends AbstractAssert<MailMessagesAssertions<S>, Message> {
    @Inject
    private static Wiser wiser;

    /**
     * Create the mail assertion.
     *
     * @param message the message to consider.
     */
    public MailMessagesAssertions(Message message) {
        super(message, MailMessagesAssertions.class);
    }

    /**
     * Assert that the message has a title.
     *
     * @return the assertion for chaining.
     */
    public MailMessagesAssertions hasTitle() {
        checkNotNull();
        try {
            assertThat(actual.getSubject()).isNotEmpty();
        } catch (MessagingException e) {
            throwException(e, NO_MAIL_SUBJECT_SPECIFIED);
        }
        return this;
    }

    /**
     * Assert that the message has a title.
     *
     * @return the assertion for chaining.
     */
    public MailMessagesAssertions hasSentDate() {
        checkNotNull();
        try {
            assertThat(actual.getSentDate()).isNotNull();
        } catch (MessagingException e) {
            throwException(e, NO_SENT_DATE_FOUND);
        }
        return this;
    }

    /**
     * Assert that the message has recipients of a specific type.
     *
     * @param type the type of recipients to check for.
     * @return the assertion for chaining.
     */
    public MailMessagesAssertions hasRecipients(RecipientType type) {
        checkNotNull();
        try {
            assertThat(actual.getRecipients(type)).isNotNull();
            assertThat(actual.getRecipients(type)).isNotEmpty();
        } catch (MessagingException e) {
            throwException(e, NO_RECIPIENTS_SPECIFIED);
        }
        return this;
    }

    /**
     * Assert that the recipients field is equal to specified recipients.
     *
     * @param type       the recipient type to check for.
     * @param recipients the recipients list to check for.
     * @return the assertion for chaining.
     */
    public MailMessagesAssertions recipientEqualsTo(final RecipientType type, final Address[] recipients) {
        final WiserMessage wiserMessage = FluentIterable.from(wiser.getMessages()).firstMatch(new Predicate<WiserMessage>() {
            @Override
            public boolean apply(@Nullable WiserMessage input) {
                try {
                    return Arrays.equals(recipients, input.getMimeMessage().getRecipients(type));
                } catch (MessagingException e) {
                    throwException(e, NOT_SAME_RECIPIENTS_SIZE);
                }
                return false;
            }
        }).orNull();
        if (wiserMessage == null) {
            fail("The Searched Message was Not Found");
        }
        return this;
    }

    /**
     * Assert the message satisfies a predicate.
     *
     * @param predicate the predicate to check for.
     * @return the assertion for chaining.
     */
    public MailMessagesAssertions checkByCondition(Predicate<Message> predicate) {
        final List<WiserMessage> messages = wiser.getMessages();
        final Collection<Message> transform = getMessages(messages);
        final Message result = FluentIterable.from(transform).firstMatch(predicate).orNull();
        if (result == null) {
            fail("message not found");
        }
        return this;
    }

    private Collection<Message> getMessages(List<WiserMessage> messages) {
        return Collections2.transform(messages, new Function<WiserMessage, Message>() {
            @Nullable
            @Override
            public Message apply(@Nullable WiserMessage input) {
                try {
                    return input.getMimeMessage();
                } catch (MessagingException e) {
                    throwException(e, ERROR_OCCURED_WHILE_EXTRACTING_MESSAGE_FROM_SERVER);
                }
                return null;
            }

        });
    }

    private MailMessagesAssertions checkNotNull() {
        assertThat(actual).isNotNull();
        return this;
    }

    /**
     * @return the messages received by the mock server;
     */
    protected Collection<Message> getMessages() {
        return getMessages(wiser.getMessages());
    }

    /**
     * Utility method to throw exceptions.
     */
    protected void throwException(Throwable e, MailAssertionsErrorCodes errorCode) {
        throw SeedException
                .wrap(e, errorCode)
                .put("more", "\n" + e.getMessage());
    }
}
