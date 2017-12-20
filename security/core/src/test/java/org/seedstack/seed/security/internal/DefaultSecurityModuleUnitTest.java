/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.PrivateBinder;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedElementBuilder;
import java.lang.annotation.Annotation;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

public class DefaultSecurityModuleUnitTest {

    private DefaultSecurityModule underTest;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testShiroModule() {
        SecurityGuiceConfigurer securityGuiceConfigurer = mock(SecurityGuiceConfigurer.class);
        underTest = new DefaultSecurityModule(securityGuiceConfigurer);

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
