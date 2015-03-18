/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.handlers.server;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.reflect.Whitebox;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class HttpBasicAuthenticationHandlerUnitTest {

    private HttpBasicAuthenticationHandler underTest;
    private org.apache.shiro.mgt.SecurityManager mockSecurityManager = mock(org.apache.shiro.mgt.SecurityManager.class);
    private SOAPMessageContext context = mock(SOAPMessageContext.class);

    @Before
    public void setUp() {
        Whitebox.setInternalState(HttpBasicAuthenticationHandler.class, "securityManager", mockSecurityManager);
        underTest = new HttpBasicAuthenticationHandler();
    }

    @Test
    public void testGetHeaders() {
        Set<QName> headers = underTest.getHeaders();
        assertThat(headers).isEmpty();
    }

    @Test
    public void handleMessage_in_outBound() {
        SOAPMessageContext messageContext = mock(SOAPMessageContext.class);
        when(messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(true);
        boolean result = underTest.handleMessage(messageContext);
        assertThat(result).isTrue();
    }

    @Test(expected = org.seedstack.seed.security.api.exceptions.AuthenticationException.class)
    public void handleMessage_in_inBound() {
        SOAPMessageContext messageContext = mock(SOAPMessageContext.class);
        when(messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
        underTest.handleMessage(messageContext);
        fail("should fail");
    }

    @Test(expected = org.seedstack.seed.security.api.exceptions.AuthenticationException.class)
    public void handleMessage_in_inBound_with_null_http_headers() {
        SOAPMessageContext messageContext = mock(SOAPMessageContext.class);
        when(messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
        when(messageContext.get(MessageContext.HTTP_REQUEST_HEADERS)).thenReturn(null);
        boolean result = underTest.handleMessage(messageContext);
        assertThat(result).isTrue();
    }

    @Test(expected = org.seedstack.seed.security.api.exceptions.AuthenticationException.class)
    public void handleMessage_in_inBound_without_basic_auth_header() {
        SOAPMessageContext messageContext = mock(SOAPMessageContext.class);
        when(messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
        when(messageContext.get(MessageContext.HTTP_REQUEST_HEADERS)).thenReturn(Collections.emptyMap());
        boolean result = underTest.handleMessage(messageContext);
        assertThat(result).isTrue();
    }

    @Test
    public void handleMessage_in_inBound_basic_auth_header() throws UnsupportedEncodingException {
        SOAPMessageContext messageContext = mock(SOAPMessageContext.class);
        when(messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        String token = "basic bG9naW46cGFzc3dvcmQ=";
        List<String> arrayList = new ArrayList<String>();
        arrayList.add(token);
        headers.put("Authorization", arrayList);
        when(messageContext.get(MessageContext.HTTP_REQUEST_HEADERS)).thenReturn(headers);
        Subject subject = mock(Subject.class);
        when(mockSecurityManager.createSubject(any(SubjectContext.class))).thenReturn(subject);
        boolean result = underTest.handleMessage(messageContext);
        assertThat(result).isTrue();
    }


    @Test
    public void handleMessage_in_inBound_basic_auth_header_login_fail() throws UnsupportedEncodingException {
        SOAPMessageContext messageContext = mock(SOAPMessageContext.class);
        final Map<Object, Object> messaMap = new HashMap<Object, Object>();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                messaMap.put(args[0], args[1]);
                return null;
            }
        }).when(messageContext).put(anyString(), anyString());

        when(messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        String token = "basic bG9naW46cGFzc3dvcmQ=";
        List<String> arrayList = new ArrayList<String>();
        arrayList.add(token);
        headers.put("Authorization", arrayList);
        when(messageContext.get(MessageContext.HTTP_REQUEST_HEADERS)).thenReturn(headers);
        Subject subject = mock(Subject.class);
        doThrow(new AuthenticationException()).when(subject).login(any(UsernamePasswordToken.class));
        when(mockSecurityManager.createSubject(any(SubjectContext.class))).thenReturn(subject);
        boolean result = underTest.handleMessage(messageContext);
        assertThat(messaMap.get(MessageContext.HTTP_RESPONSE_CODE)).isEqualTo(401);
        assertThat(result).isFalse();
    }

    @Test
    public void handleMessage_in_inBound_basic_auth_header_with_exception() throws UnsupportedEncodingException {
        SOAPMessageContext messageContext = mock(SOAPMessageContext.class);
        when(messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        String token = "basic bG9naW46cGFzc3dvcmQ=";
        List<String> arrayList = new ArrayList<String>();
        arrayList.add(token);
        headers.put("Authorization", arrayList);
        when(messageContext.get(MessageContext.HTTP_REQUEST_HEADERS)).thenReturn(headers);
        Subject subject = mock(Subject.class);
        doThrow(new RuntimeException()).when(subject).login(any(UsernamePasswordToken.class));
        when(mockSecurityManager.createSubject(any(SubjectContext.class))).thenReturn(subject);
        boolean result = underTest.handleMessage(messageContext);
        assertThat(result).isFalse();
    }

    @Test
    public void testHandleFault() {
        boolean b = underTest.handleFault(context);
        assertThat(b).isTrue();
    }
}
