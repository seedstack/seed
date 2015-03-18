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

import com.google.common.cache.LoadingCache;
import com.google.inject.assistedinject.Assisted;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WebServiceContextDelegate;

import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.ws.BindingProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

class JmsServerTransport implements WebServiceContextDelegate {
    private final LoadingCache<SoapJmsUri, Connection> connectionCache;
    private final AtomicBoolean isClosed = new AtomicBoolean();
    private final SoapJmsUri soapJmsUri;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final String requestContentType;
    private final Destination replyTo;
    private final String correlationID;
    private final String username;
    private final String password;

    private String responseContentType;
    private boolean mustReply;

    @Inject
    JmsServerTransport(LoadingCache<SoapJmsUri, Connection> connectionCache, @Assisted Message requestMessage, @Assisted SoapJmsUri soapJmsUri) throws JMSException {
        this.connectionCache = connectionCache;
        this.soapJmsUri = soapJmsUri;
        this.outputStream = new ByteArrayOutputStream();
        this.username = requestMessage.getStringProperty(BindingProvider.USERNAME_PROPERTY);
        this.password = requestMessage.getStringProperty(BindingProvider.PASSWORD_PROPERTY);
        this.requestContentType = requestMessage.getStringProperty(JmsConstants.CONTENT_TYPE_PROPERTY);
        this.replyTo = requestMessage.getJMSReplyTo();
        this.correlationID = requestMessage.getJMSCorrelationID();

        if (requestMessage instanceof BytesMessage) {
            long bodyLength = ((BytesMessage) requestMessage).getBodyLength();

            if (bodyLength > Integer.MAX_VALUE) {
                throw new IllegalStateException("JMS message too long");
            }

            byte[] rqstBuf = new byte[(int) bodyLength];
            ((BytesMessage) requestMessage).readBytes(rqstBuf);

            this.inputStream = new ByteArrayInputStream(rqstBuf);
        } else if (requestMessage instanceof TextMessage) {
            try {
                String encoding = requestMessage.getStringProperty(JmsConstants.ENCODING_PROPERTY);
                if (encoding == null || encoding.isEmpty()) {
                    encoding = "UTF-8";
                }
                this.inputStream = new ByteArrayInputStream(((TextMessage) requestMessage).getText().getBytes(encoding));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("Unable to decode JMS text message", e);
            }
        } else {
            throw new IllegalStateException("Unknown message type (only byte messages and text messages are supported)");
        }
    }

    InputStream getInputStream() {
        return inputStream;
    }

    OutputStream getOutputStream() {
        return outputStream;
    }

    void close() {
        if (isClosed.compareAndSet(false, true)) {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                throw new JmsTransportException("Unable to close JMS transport", e);
            }
        }
    }

    @Override
    public Principal getUserPrincipal(Packet request) {
        return null;
    }

    @Override
    public boolean isUserInRole(Packet request, String role) {
        return false;
    }

    @Override
    public String getEPRAddress(Packet request, WSEndpoint endpoint) {
        return soapJmsUri.toString();
    }

    @Override
    public String getWSDLAddress(Packet request, WSEndpoint endpoint) {
        return null;
    }

    void flush() throws IOException, JMSException {
        outputStream.flush();

        if (mustReply) {
            Session jmsSession;
            try {
                Connection connection = connectionCache.get(soapJmsUri); // NOSONAR
                jmsSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            } catch (ExecutionException e) {
                throw new JmsTransportException("Unable to open JMS transport for reply to " + soapJmsUri, e);
            }

            try {
                BytesMessage replyMessage = jmsSession.createBytesMessage();

                replyMessage.setJMSCorrelationID(correlationID);
                replyMessage.setStringProperty(JmsConstants.CONTENT_TYPE_PROPERTY, responseContentType);

                byte[] content = ((ByteArrayOutputStream) getOutputStream()).toByteArray();
                if (content.length > 0) {
                    replyMessage.writeBytes(content);
                }

                MessageProducer messageProducer = jmsSession.createProducer(replyTo);
                messageProducer.send(replyMessage);
                messageProducer.close();
            } finally {
                jmsSession.close();
            }
        }
    }

    String getRequestUsername() {
        return this.username;
    }

    String getRequestPassword() {
        return this.password;
    }

    String getRequestContentType() {
        return requestContentType;
    }

    void setResponseContentType(String value) {
        this.responseContentType = value;
    }

    void setMustReply(boolean mustReply) {
        this.mustReply = mustReply;
    }
}
