/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
//package org.seedstack.seed.ws.internal;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.powermock.reflect.Whitebox;
//
//import javax.servlet.ServletContext;
//import javax.xml.ws.Endpoint;
//import java.util.HashSet;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//
//public class WSPluginUnitTest {
//
//    WSPlugin underTest;
//
//    @Before
//    public  void setUp() {
//        underTest = new WSPlugin();
//    }
//
//    @Test
//    public void testName() {
//        String name = underTest.name();
//        assertThat(name).isEqualTo("seed-ws-plugin");
//    }
//
//    @Test
//    public void testStop() {
//        List<Endpoint> endpointsList =  Whitebox.getInternalState(underTest,"endpointsList");
//        final AtomicInteger counter = new AtomicInteger();
//        for (int i = 0; i <10 ; i++) {
//            Endpoint mockEndpoint = mock(Endpoint.class);
//            endpointsList.add(mockEndpoint);
//        }
//        underTest.stop();
//
//
//        for (Endpoint endpoint : endpointsList) {
//            verify(endpoint,times(1)).stop();
//        }
//
//    }
//
//    @Test
//    public void test_provideContainerContext_with_null_context() {
//        underTest.provideContainerContext(null);
//       Boolean result = Whitebox.getInternalState(underTest,"isWebapp");
//        assertThat(result).isFalse();
//    }
//
//    @Test
//    public void test_provideContainerContext_with_array_of_string_context() {
//        underTest.provideContainerContext(new String []{"args1","args2","args3"});
//        Boolean result = Whitebox.getInternalState(underTest,"isWebapp");
//        assertThat(result).isFalse();
//    }
//
//    @Test
//    public void test_provideContainerContext_with_servletContext_context() {
//        underTest.provideContainerContext(mock(ServletContext.class));
//        Boolean result = Whitebox.getInternalState(underTest,"isWebapp");
//        assertThat(result).isTrue();
//    }
//
//    @Test
//    public void nativeUnitModule_with_isWebapp_false() {
//         Whitebox.setInternalState(underTest,"isWebapp",false);
//        Whitebox.setInternalState(underTest,"webServiceClasses",new HashSet<Class<?>>());
//        Whitebox.setInternalState(underTest,"webServiceClientClasses",new HashSet<Class<?>>());
//
//        Object result = underTest.nativeUnitModule();
//        assertThat(result).isInstanceOf(WSModule.class);
//    }
//
//
//
//}
