/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.spring;


import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.seedstack.seed.spring.fixtures.DummyService;
import org.seedstack.seed.spring.fixtures.Service;
import io.nuun.kernel.api.Kernel;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Proxy;

import static io.nuun.kernel.core.NuunCore.createKernel;
import static io.nuun.kernel.core.NuunCore.newKernelConfiguration;

public class SeedFactoryBeanIT {
    static Kernel underTest;
    ApplicationContext context;
    Holder holder;

    static class Holder {
        @Inject
        Service service;

        @Inject
        @Named("service1")
        Service springService1;

        @Inject
        @Named("service2")
        Service springService2;
    }


    @BeforeClass
    public static void beforeClass() throws Exception {
        underTest = createKernel(newKernelConfiguration());
        underTest.init();
        underTest.start();
    }

    @AfterClass
    public static void afterClass() {
        underTest.stop();
    }

    @Before
    public void before() {
        Module aggregationModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Holder.class);
            }
        };
        holder = underTest.objectGraph().as(Injector.class).createChildInjector(aggregationModule).getInstance(Holder.class);
        context = new ClassPathXmlApplicationContext("META-INF/spring/SeedFactoryBeanIT-context.xml");
    }

    @Test
    public void can_retrieve_resource_from_spring_context() {
        Assertions.assertThat(holder.service).isNotNull();
        Assertions.assertThat(holder.springService1).isNotNull();
        Assertions.assertThat(holder.springService2).isNotNull();
        Assertions.assertThat(holder.springService1.equals(holder.service)).isTrue();
        Assertions.assertThat(holder.springService2.equals(holder.service)).isTrue();
    }

    @Test
    public void singletons_are_of_same_instance() {
        Assertions.assertThat(context.getBean("service1")).isEqualTo(context.getBean("service2"));
        Assertions.assertThat(context.getBean("service1")).isEqualTo(holder.springService1);
        Assertions.assertThat(context.getBean("service2")).isEqualTo(holder.springService2);
    }

    @Test
    public void proxy_attribute_is_applied() {
        Assertions.assertThat(Proxy.isProxyClass(context.getBean("service2").getClass())).isEqualTo(true);
        Assertions.assertThat(Proxy.isProxyClass(context.getBean("service3").getClass())).isEqualTo(false);
    }

    @Test
    public void configuration_injection_is_working() {
        Assertions.assertThat(((DummyService) context.getBean("service4")).getConfigurationValue()).isEqualTo("Hello");
        Assertions.assertThat(((DummyService) context.getBean("service4")).getOtherConfigurationValue()).isEqualTo(5);
        Assertions.assertThat(((DummyService) context.getBean("service4")).getDefaultConfigurationValue()).isEqualTo(1.5);
    }
}
