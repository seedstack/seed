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
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Simple implementation of the @Link MessageRetriever.
 * <p>
 * Created by E442250 on 19/05/2014.
 */
class WiserMessageRetriever implements MessageRetriever {
    @Inject
    private Wiser wiser;

    @Override
    public Collection<Message> getSentMessages() {
        if (wiser != null) {
            List<Message> result = new ArrayList<Message>();
            for (WiserMessage wiserMessage : wiser.getMessages()) {
                Message message;

                try {
                    message = wiserMessage.getMimeMessage();
                } catch (MessagingException e) {
                    throw new RuntimeException("Unable to retrieve sent messages", e);
                }

                if (message != null) {
                    result.add(message);
                }
            }

            return result;
        }

        return Lists.newArrayList();
    }
}
