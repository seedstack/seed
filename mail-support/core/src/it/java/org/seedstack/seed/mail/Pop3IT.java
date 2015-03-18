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
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

public class Pop3IT extends AbstractSeedIT {
    @Inject
    @Named("pop3-test")
    Session pop3Session;

    @Before
    public void setUp() throws Exception {
        pop3Session.setDebug(true);
    }

    /**
     * just prints the configured properties for the session using POP protocol, these properties
     * has to be configured by the user in a .props file
     */
    @Test
    public void test_configure_pop_session() {
        Assertions.assertThat(pop3Session).isNotNull();
        System.out.println(pop3Session.getProperties().toString());
    }


    /**
     * this method is expected to fail as there is no host configured for pop
     *
     * @throws javax.mail.MessagingException
     */
    @Test(expected = MessagingException.class)
    @Ignore
    public void test_read_mailbox_with_pop3() throws MessagingException {
        final Store store = pop3Session.getStore();
        store.connect(TestConstantsValues.DEFAULT_USERNAME, TestConstantsValues.DEFAULT_PASSWORD);

        final Folder defaultFolder = store.getDefaultFolder();
        Assertions.assertThat(defaultFolder.getFullName());
    }
}
