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

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;

class MailPreparator {
    private Session session;

    MailPreparator(Session session) {
        this.session = session;
    }

    Message prepareMessageToBeSent(String to, String from, String subject, String text) throws MessagingException {
        Message m = new MimeMessage(session);
        m.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        m.setFrom(new InternetAddress(from));
        m.setSubject(subject);
        m.setText(text);
        // m.setContent("Test Content","plain/text");
        m.setSentDate(new Date());
        return m;
    }
}
