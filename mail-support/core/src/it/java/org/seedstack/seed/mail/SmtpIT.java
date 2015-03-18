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

import org.seedstack.seed.it.AbstractSeedIT;
import org.seedstack.seed.mail.api.MessageRetriever;
import org.seedstack.seed.mail.api.WithMailServer;
import org.seedstack.seed.mail.assertions.MockMailServerAssertions;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;

import static org.assertj.core.api.Assertions.assertThat;

@WithMailServer(host = "localhost", port = 6457)
public class SmtpIT extends AbstractSeedIT {
    @Inject
    @Named("smtp-test")
    Session smtpSession;

    @Inject
    MessageRetriever retriever;

    MailPreparator mailer = new MailPreparator(smtpSession);

    /**
     * just prints the configured properties for the session using SMTP protocol, these properties
     * has to be configured by the user in a .props file
     */
    @Test
    public void test_configure_smtp_session() {
        Assertions.assertThat(smtpSession).isNotNull();
        assertThat(smtpSession.getProperty("mail.smtp.host")).isEqualTo("localhost");
        assertThat(smtpSession.getProperty("mail.smtp.port")).isEqualTo("6457");
    }

    @Test
    public void test_send() throws MessagingException {
        Transport transport = null;
        try {
            transport = smtpSession.getTransport();
            transport.connect();
            Message message = mailer.prepareMessageToBeSent(TestConstantsValues.DEFAULT_RECIPIENT, TestConstantsValues.DEFAULT_FROM, "test", "\"Envoie Mail à partir du plugin JavaMail\"");
            transport.sendMessage(message, message.getAllRecipients());
        } finally {
            if (transport != null) {
                transport.close();
            }
        }

        for (Message message : retriever.getSentMessages()) {
            MockMailServerAssertions.assertThat(message).hasRecipients(Message.RecipientType.TO);
            MockMailServerAssertions.assertThat(message).recipientEqualsTo(Message.RecipientType.TO, InternetAddress.parse(TestConstantsValues.DEFAULT_RECIPIENT));
        }
    }
}
