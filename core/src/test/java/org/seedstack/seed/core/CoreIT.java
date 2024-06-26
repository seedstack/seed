/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import com.google.inject.ConfigurationException;
import com.google.inject.CreationException;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.aopalliance.intercept.MethodInvocation;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Bind;
import org.seedstack.seed.Logging;
import org.seedstack.seed.Nullable;
import org.seedstack.seed.Provide;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.SeedInterceptor;
import org.seedstack.seed.core.fixtures.BoundFromInterface;
import org.seedstack.seed.core.fixtures.BoundFromInterfaceWithName;
import org.seedstack.seed.core.fixtures.BoundFromItself;
import org.seedstack.seed.core.fixtures.BoundInterface;
import org.seedstack.seed.core.fixtures.BoundOverrideFromInterfaceWithAnnotation;
import org.seedstack.seed.core.fixtures.Dummy;
import org.seedstack.seed.core.fixtures.DummyService1;
import org.seedstack.seed.core.fixtures.DummyService2;
import org.seedstack.seed.core.fixtures.DummyService3;
import org.seedstack.seed.core.fixtures.ProvidedInterface;
import org.seedstack.seed.core.fixtures.Service;
import org.seedstack.seed.core.fixtures.Service1;
import org.seedstack.seed.core.fixtures.Service2;
import org.seedstack.seed.core.fixtures.Service3;
import org.seedstack.seed.core.fixtures.TestSeedInitializer;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import some.other.pkg.ForeignClass;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(SeedITRunner.class)
public class CoreIT {
    @Inject
    private Injector injector;

    @Test
    public void initializersAreCalled() {
        assertThat(TestSeedInitializer.getBeforeCallCount()).isEqualTo(1);
        assertThat(TestSeedInitializer.getOnCallCount()).isEqualTo(1);
        assertThat(TestSeedInitializer.getAfterCallCount()).isEqualTo(1);
    }

    @Test
    public void modulesAreInstalledCorrectly() {
        HolderNominal holder = injector.getInstance(HolderNominal.class);

        assertThat(holder).isNotNull();
        assertThat(holder.s1).isInstanceOf(DummyService1.class);
        assertThat(holder.s3).isNull();
        assertThat(holder.s3bis).isInstanceOf(DummyService1.class);
        assertThat(holder.overriding).isInstanceOf(DummyService2.class);
        assertThat(holder.overridingNothing).isInstanceOf(DummyService3.class);
    }

    @Test(expected = ConfigurationException.class)
    public void modulesWithoutInstallAreNotInstalledCorrectly() {
        injector.getInstance(HolderException.class);
    }

    @Test
    public void loggerInjectionIsWorking() {
        LoggerHolder holder = injector.getInstance(LoggerHolder.class);
        assertThat(LoggerHolder.logger).isNotNull();
        assertThat(holder.logger1).isSameAs(LoggerHolder.logger);
    }

    @Test
    public void loggerInjectionWithSubclasses() {
        SubLoggerHolder1 subHolder1 = injector.getInstance(SubLoggerHolder1.class);
        SubLoggerHolder2 subHolder2 = injector.getInstance(SubLoggerHolder2.class);

        assertThat(subHolder1.logger2).isNotNull();
        assertThat(subHolder1.logger1).isNotNull();
        assertThat(subHolder2.logger2).isNotNull();
        assertThat(subHolder2.logger1).isNotNull();
    }

    @Test(expected = CreationException.class)
    public void loggerInjectionThrowsErrorOnUnexpectedType() {
        try {
            injector.createChildInjector((Module) binder -> binder.bind(BadLoggerHolder.class));
        } catch (CreationException e) {
            Throwable cause = e.getCause();
            assertThat(cause).isInstanceOf(SeedException.class);
            assertThat(((SeedException) cause).getErrorCode()).isEqualTo(CoreErrorCode.BAD_LOGGER_TYPE);
            throw e;
        }
        fail("should have failed");
    }

    @Test
    public void multiplePackageRootsCanBeUsed() {
        HolderNominal holder = injector.getInstance(HolderNominal.class);
        assertThat(holder.foreignClass).isNotNull();
    }

    @Test
    public void explicitBindingsAreWorking() {
        HolderNominal holder = injector.getInstance(HolderNominal.class);
        assertThat(holder.boundFromItself).isInstanceOf(BoundFromItself.class);
        assertThat(holder.boundFromInterface).isInstanceOf(BoundFromInterface.class);
        assertThat(holder.boundFromInterfaceWithName).isInstanceOf(BoundFromInterfaceWithName.class);
        assertThat(holder.boundFromInterfaceWithAnnotation).isInstanceOf(
                BoundOverrideFromInterfaceWithAnnotation.class);
    }

    @Test
    public void explicitProvidedBindingsAreWorking() {
        HolderNominal holder = injector.getInstance(HolderNominal.class);
        assertThat(holder.providedFromInterface).isInstanceOf(ProvidedInterface.class);
        assertThat(holder.providedFromInterfaceWithName).isInstanceOf(ProvidedInterface.class);
        assertThat(holder.providedFromInterfaceWithAnnotation).isInstanceOf(ProvidedInterface.class);
    }

    @Test
    public void methodInterceptorsAreWorking() {
        InterceptionTarget instance = injector.getInstance(InterceptionTarget.class);
        assertThat(SomeSeedInterceptor.invokedTimes).isEqualTo(0);
        instance.someMethod();
        assertThat(SomeSeedInterceptor.invokedTimes).isEqualTo(1);
        instance.otherMethod();
        assertThat(SomeSeedInterceptor.invokedTimes).isEqualTo(1);
    }

    @Test
    public void annotatedProviderIsWorking() {
        HolderNominal instance = injector.getInstance(HolderNominal.class);
        assertThat(instance.testValue).isEqualTo(Lists.newArrayList("test"));
    }

    @Provide
    @Named("test")
    private static class ValueProvider implements Provider<List<String>> {
        @Override
        public List<String> get() {
            return Lists.newArrayList("test");
        }
    }

    @Bind
    private static class LoggerHolder {
        private static final Logger logger = LoggerFactory.getLogger(LoggerHolder.class);
        @Logging
        Logger logger1;
    }

    private static class BadLoggerHolder {
        @Logging
        private Object logger;
    }

    @Bind
    private static class SubLoggerHolder1 extends LoggerHolder {
        @Logging
        private Logger logger2;
    }

    @Bind
    private static class SubLoggerHolder2 extends LoggerHolder {
        @Logging
        private Logger logger2;
    }

    @Bind
    private static class HolderNominal {
        @Inject
        Service1 s1;
        @Inject
        @Nullable
        Service3 s3;
        @Inject
        @Named("Service3Bis")
        Service s3bis;
        @Inject
        @Named("Overriding")
        Service overriding;
        @Inject
        @Named("OverridingNothing")
        Service overridingNothing;
        @Inject
        ForeignClass foreignClass;
        @Inject
        BoundFromItself boundFromItself;
        @Inject
        BoundInterface boundFromInterface;
        @Inject
        @Named("toto")
        BoundInterface boundFromInterfaceWithName;
        @Inject
        @Dummy
        BoundInterface boundFromInterfaceWithAnnotation;
        @Inject
        ProvidedInterface<Integer> providedFromInterface;
        @Inject
        @Named("toto")
        ProvidedInterface<Integer> providedFromInterfaceWithName;
        @Inject
        @Dummy
        ProvidedInterface<Integer> providedFromInterfaceWithAnnotation;
        @Inject
        @Named("test")
        List<String> testValue;
    }

    private static class HolderException {
        @Inject
        Service2 s2;
    }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface ShouldBeIntercepted {

    }

    @Bind
    static class InterceptionTarget {
        @ShouldBeIntercepted
        public void someMethod() {

        }

        public void otherMethod() {

        }
    }

    private static class SomeSeedInterceptor implements SeedInterceptor {
        static int invokedTimes = 0;
        @Logging
        private Logger logger;

        @Override
        public Predicate<Class<?>> classPredicate() {
            return InterceptionTarget.class::isAssignableFrom;
        }

        @Override
        public Predicate<Method> methodPredicate() {
            return m -> m.isAnnotationPresent(ShouldBeIntercepted.class);
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            assertThat(logger).isNotNull();
            invokedTimes++;
            return invocation.proceed();
        }
    }
}
