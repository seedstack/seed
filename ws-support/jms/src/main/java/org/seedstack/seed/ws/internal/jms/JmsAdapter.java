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

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.Adapter;
import com.sun.xml.ws.api.server.TransportBackChannel;
import com.sun.xml.ws.api.server.WSEndpoint;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.InputStream;

class JmsAdapter extends Adapter<JmsAdapter.JMSToolkit> {
    @Inject
    private WSJmsTransportFactory wsJmsTransportFactory;

    JmsAdapter(WSEndpoint endpoint) {
        super(endpoint);
    }

    void handle(Message requestMessage, SoapJmsUri uri) throws IOException, JMSException {
        JmsServerTransport connection = wsJmsTransportFactory.createJmsServerTransport(requestMessage, uri);

        JMSToolkit tk = pool.take();
        try {
            tk.handle(connection);
            connection.flush();
        } finally {
            pool.recycle(tk);
            connection.close();
        }
    }

    @Override
    protected JMSToolkit createToolkit() {
        return new JMSToolkit();
    }

    final class JMSToolkit extends Adapter.Toolkit implements TransportBackChannel {
        private JmsServerTransport connection;

        private void handle(JmsServerTransport connection) throws IOException {
            this.connection = connection;

            String contentTypeStr = connection.getRequestContentType();
            InputStream in = connection.getInputStream();
            Packet packet = new Packet();
            codec.decode(in, contentTypeStr, packet);

            packet.invocationProperties.put(BindingProvider.USERNAME_PROPERTY, connection.getRequestUsername());
            packet.invocationProperties.put(BindingProvider.PASSWORD_PROPERTY, connection.getRequestPassword());

            try {
                packet = head.process(packet, connection, this);
            } catch (Exception e) {
                throw new WebServiceException("Error during message processing", e);
            }

            if (packet.getMessage() != null) {
                connection.setMustReply(true);
                contentTypeStr = codec.getStaticContentType(packet).getContentType();

                if (contentTypeStr == null) {
                    throw new UnsupportedOperationException();
                } else {
                    connection.setResponseContentType(contentTypeStr);
                    codec.encode(packet, connection.getOutputStream());
                }
            }
        }

        @Override
        public void close() {
            connection.close();
        }
    }
}
