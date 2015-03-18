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

import org.seedstack.seed.core.api.Logging;
import org.seedstack.seed.it.AbstractSeedIT;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import static org.assertj.core.api.Assertions.assertThat;

public class ImapIT extends AbstractSeedIT {
    @Logging
    Logger logger;

    @Inject
    @Named("imap-test")
    Session imapSession;

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        imapSession.setDebug(true);
    }

    /**
     * just prints the configured properties for the session using IMAP protocol, these properties
     * has to be configured by the user in a .props file
     */
    @Test
    public void test_configure_imap_session() {
        Assertions.assertThat(imapSession).isNotNull();
        assertThat(imapSession.getProperty("mail.imap.user")).isEqualTo("aymen.benhmida@ext.mpsa.com");
        assertThat(imapSession.getProperty("mail.imap.host")).isEqualTo("testserver");
        assertThat(imapSession.getProperty("mail.imap.auth.login.disable")).isEqualTo(Boolean.FALSE.toString());
        assertThat(imapSession.getProperty("mail.imap.auth.plain.disable")).isEqualTo(Boolean.TRUE.toString());
    }

    @Test
    @Ignore
    public void test_read_mailbox_with_imap() throws MessagingException {
        final Store store = imapSession.getStore();
        store.connect(TestConstantsValues.DEFAULT_USERNAME, TestConstantsValues.DEFAULT_PASSWORD);

        final Folder defaultFolder = store.getDefaultFolder();
        Assertions.assertThat(defaultFolder.getFullName());

        final Folder inbox = defaultFolder.getFolder("INBOX");
        logger.info("Name:     {} ", inbox.getName());
        logger.info("Full Name: {} ", inbox.getFullName());
        logger.info("URL:    {}   ", inbox.getURLName());
    }
}
