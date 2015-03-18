/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.handlers.client;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Collections;
import java.util.Set;

/**
 * Add the necessary WS-Security UsernameToken policy information to WS clients outbound requests.
 *
 * @author emmanuel.vinel@mpsa.com
 */
public final class WSSUsernameTokenHandler implements SOAPHandler<SOAPMessageContext> {
    private final String login;
    private final String password;

    /**
     * Creates the handler.
     *
     * @param login    the supplied login.
     * @param password the supplied password.
     */
    public WSSUsernameTokenHandler(String login, String password) {
        super();
        this.login = login;
        this.password = password;
    }

    @Override
    public boolean handleMessage(final SOAPMessageContext messageContext) {
        final Boolean outbound = (Boolean) messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (outbound) {
            try {
                SOAPEnvelope envelope = messageContext.getMessage().getSOAPPart().getEnvelope();
                SOAPHeader header = envelope.getHeader();
                if (header == null) {
                    header = envelope.addHeader();
                }

                final SOAPElement security = header.addChildElement("Security", "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
                final SOAPElement userToken = security.addChildElement("UsernameToken", "wsse");
                userToken.addChildElement("Username", "wsse").addTextNode(login);
                userToken.addChildElement("Password", "wsse").addTextNode(password);
            } catch (SOAPException e) {
                throw new RuntimeException("Unable to set WS-Security username token headers", e);
            }
        }
        return true;
    }

    @Override
    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {
        // nothing to do here
    }
}