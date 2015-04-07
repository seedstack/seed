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

import com.google.inject.assistedinject.Assisted;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

class JmsTransportTube extends AbstractTubeImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(JmsTransportTube.class);

    private final Codec codec;
    private final QName serviceName;

    @Inject
    private WSJmsTransportFactory wsJmsTransportFactory;

    @Inject
    JmsTransportTube(@Assisted Codec codec, @Assisted QName serviceName) {
        this.codec = codec;
        this.serviceName = serviceName;
    }

    protected JmsTransportTube(JmsTransportTube that, TubeCloner cloner) {
        super(that, cloner);
        this.serviceName = that.serviceName;
        this.codec = that.codec.copy();
    }

    @Override
    public void preDestroy() {
        // nothing to do here
    }

    @Override
    public JmsTransportTube copy(TubeCloner cloner) {
        return new JmsTransportTube(this, cloner);
    }

    @Override
    @SuppressWarnings("unchecked")
    public NextAction processRequest(Packet request) {
        ByteArrayInputStream replyPacketInStream = null;
        ByteArrayOutputStream requestPacketOutStream = null;

        try {
            Map<String, String> reqHeaders = (Map<String, String>) request.invocationProperties.get(JmsConstants.JMS_REQUEST_HEADERS);

            if (reqHeaders == null) {
                reqHeaders = new HashMap<String, String>();
            }

            JmsClientTransport con = wsJmsTransportFactory.createJmsClientTransport(request, reqHeaders);

            ContentType ct = codec.getStaticContentType(request);
            requestPacketOutStream = new ByteArrayOutputStream();
            ContentType dynamicCT = codec.encode(request, requestPacketOutStream);
            if (ct == null) {
                // data size is available, set it as Content-Length
                ct = dynamicCT;
            }

            String soapActionHeader = ct.getSOAPActionHeader();
            if (soapActionHeader != null) {
                reqHeaders.put(JmsConstants.SOAP_ACTION, soapActionHeader);
            }
            reqHeaders.put(JmsConstants.CONTENT_TYPE_PROPERTY, ct.getContentType());
            reqHeaders.put(JmsConstants.REQUEST_URI, buildRequestURI(request.endpointAddress.getURI()));
            reqHeaders.put(JmsConstants.TARGET_SERVICE, serviceName.toString());
            reqHeaders.put(JmsConstants.BINDING_VERSION, "1.0");

            byte[] rplPacket = con.sendMessage(requestPacketOutStream.toByteArray());

            if (rplPacket == null) {
                return doReturnWith(request.createClientResponse(null));    // one way. null response given.
            }

            String contentTypeStr = con.getResponseContentType();
            Packet reply = request.createClientResponse(null);
            replyPacketInStream = new ByteArrayInputStream(rplPacket);
            codec.decode(replyPacketInStream, contentTypeStr, reply);

            return doReturnWith(reply);
        } catch (WebServiceException wex) {
            throw wex;
        } catch (Exception ex) {
            throw new WebServiceException(ex);
        } finally {
            if (requestPacketOutStream != null) {
                try {
                    requestPacketOutStream.close();
                    if (replyPacketInStream != null) {
                        replyPacketInStream.close();
                    }
                } catch (IOException e) {
                    LOGGER.warn("Unable to close I/O buffers after WS send/receive", e);
                }
            }
        }
    }


    @Override
    public NextAction processResponse(Packet response) {
        throw new AssertionError();
    }

    @Override
    public NextAction processException(Throwable t) {
        throw new AssertionError();
    }

    private String buildRequestURI(URI source) {
        String sourceAsString = source.toASCIIString();
        int queryIndex = sourceAsString.indexOf("?");

        if (queryIndex != -1) {
            return sourceAsString.substring(0, queryIndex);
        } else {
            return source.toASCIIString();
        }
    }
}
