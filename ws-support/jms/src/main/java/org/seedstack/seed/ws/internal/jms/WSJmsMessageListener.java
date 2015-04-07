/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.internal.jms;

import com.google.inject.MembersInjector;
import org.seedstack.seed.core.api.SeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

class WSJmsMessageListener implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(WSJmsMessageListener.class);

    private final JmsAdapter adapter;
    private final SoapJmsUri soapJmsUri;
    private final Session session;
    private final boolean isTransacted;

    WSJmsMessageListener(SoapJmsUri soapJmsUri, JmsAdapter adapter, Session session) {
        this.adapter = adapter;
        this.soapJmsUri = soapJmsUri;
        this.session = session;

        try {
            this.isTransacted = session.getTransacted();
        } catch (JMSException e) {
            throw SeedException.wrap(e, WSJmsErrorCodes.UNABLE_TO_GET_TRANSACTED_STATUS);
        }
    }

    @Inject
    private void injectAdapter(MembersInjector<JmsAdapter> jmsAdapterMembersInjector) {
        jmsAdapterMembersInjector.injectMembers(this.adapter);
    }

    @Override
    public void onMessage(Message message) {
        try {
            adapter.handle(message, soapJmsUri);
            if (isTransacted) {
                session.commit();
            }
        } catch (Exception e) {
            LOGGER.error("An exception occurred during WS JMS message handling", e);

            if (isTransacted) {
                try {
                    session.rollback();
                } catch (JMSException e1) {
                    throw SeedException.wrap(e, WSJmsErrorCodes.UNABLE_TO_ROLLBACK_WS_JMS_MESSAGE);
                }
            }
        }
    }
}
