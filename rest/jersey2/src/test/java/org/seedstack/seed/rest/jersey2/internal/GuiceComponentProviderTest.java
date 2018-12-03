/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.jersey2.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.google.common.collect.Sets;
import com.google.inject.Injector;
import io.nuun.kernel.api.annotations.Ignore;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.BindingBuilder;
import org.glassfish.hk2.utilities.binding.ServiceBindingBuilder;
import org.junit.Test;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.seedstack.seed.SeedException;

public class GuiceComponentProviderTest {
    @Tested
    private GuiceComponentProvider underTest;
    @Mocked
    private ServiceLocator serviceLocator;
    @Mocked
    private ServletContext servletContext;
    @Mocked
    private Injector injector;
    @Mocked
    private GuiceIntoHK2Bridge guiceIntoHK2Bridge;
    @Mocked
    private DynamicConfiguration dynamicConfiguration;

    @Test
    public void testInitializeSaveTheServiceLocator() {
        givenServiceLocator();

        underTest.initialize(serviceLocator);

        ServiceLocator initializedServiceLocator = Deencapsulation.getField(underTest, ServiceLocator.class);
        assertThat(initializedServiceLocator).isEqualTo(serviceLocator);
    }

    @Test
    public void testGetTheInjector() {
        givenServiceLocator();

        underTest.initialize(serviceLocator);

        Injector initializedInjector = Deencapsulation.getField(underTest, Injector.class);
        assertThat(initializedInjector).isEqualTo(injector);
    }

    private void givenServiceLocator() {
        new Expectations() {{
            serviceLocator.getService(ServletContext.class);
            result = servletContext;
            servletContext.getAttribute(Injector.class.getName());
            result = injector;
            serviceLocator.getService(GuiceIntoHK2Bridge.class);
            result = guiceIntoHK2Bridge;
        }};
    }

    @Test
    public void testMissingServletContext() {
        givenMissingServletContext();
        try {
            underTest.initialize(serviceLocator);
            fail();
        } catch (SeedException e) {
            assertThat(e.getErrorCode()).isEqualTo(Jersey2ErrorCode.MISSING_SERVLET_CONTEXT);
        }
    }

    private void givenMissingServletContext() {
        new Expectations() {{
            serviceLocator.getService(ServletContext.class);
            result = null;
        }};
    }

    @Test
    public void testMissingInjector() {
        givenServletContextAndMissingInjector();
        try {
            underTest.initialize(serviceLocator);
            fail();
        } catch (SeedException e) {
            assertThat(e.getErrorCode()).isEqualTo(Jersey2ErrorCode.MISSING_INJECTOR);
        }
    }

    private void givenServletContextAndMissingInjector() {
        new Expectations() {{
            serviceLocator.getService(ServletContext.class);
            result = servletContext;
            servletContext.getAttribute(Injector.class.getName());
            result = null;
        }};
    }

    @Test
    public void testPrepareHK2Bridge(@Mocked final GuiceBridge guiceBridge) {
        givenServiceLocator();
        new Expectations() {{
            serviceLocator.getService(GuiceIntoHK2Bridge.class);
            result = guiceIntoHK2Bridge;
        }};

        underTest.initialize(serviceLocator);

        new Verifications() {{
            guiceBridge.initializeGuiceBridge(serviceLocator);
            serviceLocator.getService(GuiceIntoHK2Bridge.class);
            guiceIntoHK2Bridge.bridgeGuiceInjector(injector);
        }};
    }

    @Test
    public void testDoNotBindNonResourceClasses() {
        boolean isBound = underTest.bind(String.class, null);
        assertThat(isBound).isFalse();
    }

    @Test
    public <T> void testBindResourceClasses(@Mocked ServiceBindingBuilder<T> bindingBuilder) {
        givenServiceLocator();
        givenInjections(bindingBuilder);
        underTest.initialize(serviceLocator);

        @Ignore
        @Path("/")
        class MyResource {
        }

        assertThat(underTest.bind(MyResource.class, null)).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    public <T> void testBind(@Mocked ServiceBindingBuilder<T> bindingBuilder) {
        givenServiceLocator();
        givenInjections(bindingBuilder);
        underTest.initialize(serviceLocator);

        underTest.bind(MyProvider.class, Sets.newHashSet(MyProviderImpl.class));

        new Verifications() {{
            bindingBuilder.to(MyProviderImpl.class);
        }};
    }

    private <T> void givenInjections(ServiceBindingBuilder<T> bindingBuilder) {
        new MockUp<GuiceComponentProvider>() {
            @Mock
            DynamicConfiguration getConfiguration(final ServiceLocator locator) {
                return dynamicConfiguration;
            }

            @Mock
            ServiceBindingBuilder<T> newFactoryBinder(final Factory<T> factory) {
                return bindingBuilder;
            }

            @Mock
            void addBinding(final BindingBuilder<T> builder, final DynamicConfiguration configuration) {
            }
        };
    }

    @Ignore
    @Provider
    interface MyProvider {
    }

    class MyProviderImpl implements MyProvider {
    }
}
