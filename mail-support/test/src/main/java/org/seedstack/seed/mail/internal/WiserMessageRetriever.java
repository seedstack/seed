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

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.seedstack.seed.mail.api.MessageRetriever;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.Collection;
import java.util.List;

/**
 * Simple implementation of the @Link MessageRetriever.
 * <p/>
 * Created by E442250 on 19/05/2014.
 */
class WiserMessageRetriever implements MessageRetriever {
    @Inject
    private Wiser wiser;

    @Override
    public Collection<Message> getSentMessages() {
        if (wiser != null) {
            final List<WiserMessage> messages = wiser.getMessages();
            return Collections2.transform(messages, new Function<WiserMessage, Message>() {
                @Nullable
                @Override
                public Message apply(@Nullable WiserMessage input) {
                    try {
                        if (input != null) {
                            return input.getMimeMessage();
                        }
                    } catch (MessagingException e) {
                        throw new RuntimeException("Unable to retrieve sent messages", e);
                    }

                    return null;
                }
            });
        }
        return Lists.newArrayList();
    }
}
