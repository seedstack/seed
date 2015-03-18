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

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

class WSJmsMessageListener implements MessageListener {
    private final JmsAdapter adapter;
    private final SoapJmsUri soapJmsUri;
    private final Session session;

    WSJmsMessageListener(Session session, SoapJmsUri soapJmsUri, JmsAdapter adapter) {
        this.session = session;
        this.adapter = adapter;
        this.soapJmsUri = soapJmsUri;
    }

    @Inject // NOSONAR
    private void injectAdapter(MembersInjector<JmsAdapter> jmsAdapterMembersInjector) {
        jmsAdapterMembersInjector.injectMembers(this.adapter);
    }

    @Override
    public void onMessage(Message message) {
        try {
            adapter.handle(message, soapJmsUri);
            session.commit();
        } catch (Exception e) {
            try {
                session.rollback();
            } catch (JMSException e1) {
                e1.initCause(e);
                throw SeedException.wrap(e1, WSJmsErrorCode.UNABLE_TO_ROLLBACK_WS_JMS_MESSAGE);
            }

            throw SeedException.wrap(e, WSJmsErrorCode.UNABLE_TO_HANDLE_WS_JMS_MESSAGE);
        }
    }
}
