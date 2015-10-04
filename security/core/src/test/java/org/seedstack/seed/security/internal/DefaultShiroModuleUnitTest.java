/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;

import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import com.google.inject.PrivateBinder;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedElementBuilder;

public class DefaultShiroModuleUnitTest {

    private DefaultShiroModule underTest;
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
    public void testShiroModule(){
    	underTest = new DefaultShiroModule();
    	PrivateBinder b = mock(PrivateBinder.class);
    	AnnotatedBindingBuilder ab = mock(AnnotatedBindingBuilder.class);
    	when(b.bind(any(Class.class))).thenReturn(ab);
    	when(ab.annotatedWith(any(Annotation.class))).thenReturn(ab);
    	AnnotatedElementBuilder aeb = mock(AnnotatedElementBuilder.class);
    	when(b.expose(any(Class.class))).thenReturn(aeb);
    	Whitebox.setInternalState(underTest, "binder", b);
    	
    	underTest.configureShiro();
    }
}
