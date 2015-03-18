/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.mail.internal;

import com.google.common.collect.Lists;
import org.seedstack.seed.mail.api.MessageRetriever;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import javax.mail.Message;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WiserMessageRetrieverTest {
    @Mock
    Wiser wiser;

    private MessageRetriever messageRetriever;

    @Before
    public void setUp() {
        messageRetriever = new WiserMessageRetriever();
    }

    @Test
    public void test_get_sent_messages_with_empty_list() throws Exception {
        when(wiser.getMessages()).thenReturn(Lists.<WiserMessage>newArrayList());
        final Collection<Message> sentMessages = messageRetriever.getSentMessages();
        assertThat(sentMessages).isNotNull();
        assertThat(sentMessages).isEmpty();
    }

    @Test
    public void test_get_sent_messages_with_null_list() throws Exception {
        when(wiser.getMessages()).thenReturn(null);
        final Collection<Message> sentMessages = messageRetriever.getSentMessages();
        assertThat(sentMessages).isNotNull();
        assertThat(sentMessages).isEmpty();
    }
}