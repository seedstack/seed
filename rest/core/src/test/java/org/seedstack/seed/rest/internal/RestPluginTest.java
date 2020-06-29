/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.junit.Test;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.provider.CompositeProvider;
import org.seedstack.seed.Application;
import org.seedstack.seed.Ignore;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.configuration.PrioritizedProvider;
import org.seedstack.seed.core.internal.diagnostic.DiagnosticManagerImpl;
import org.seedstack.seed.spi.ApplicationProvider;

public class RestPluginTest {
    @Tested
    private RestPlugin underTest;
    @Mocked
    private InitContext initContext;
    @Mocked
    private ApplicationProvider applicationProvider;
    @Mocked
    private Application application;
    @Injectable
    private SeedRuntime seedRuntime;
    @Mocked
    private ServletContext servletContext;
    @Mocked
    private Coffig coffig;

    @Test
    public void testName() {
        assertThat(underTest.name()).isEqualTo("rest");
    }

    @Test
    public void testRequestScanForJaxRsClasses() {
        assertScanPredicate(JaxRsProviderPredicate.INSTANCE);
        assertScanPredicate(JaxRsResourcePredicate.INSTANCE);
    }

    private void assertScanPredicate(Predicate<Class<?>> predicate) {
        boolean scanPredicate = false;
        for (ClasspathScanRequest classpathScanRequest : underTest.classpathScanRequests()) {
            if (classpathScanRequest.classPredicate == predicate) {
                scanPredicate = true;
                break;
            }
        }
        assertThat(scanPredicate).isTrue();
    }

    @Test
    public void testWithoutServletContext() {
        givenConfiguration();
        givenSpecifications(
                new Pair<>(JaxRsResourcePredicate.INSTANCE, MyResource.class),
                new Pair<>(JaxRsProviderPredicate.INSTANCE, MyProvider.class)
        );
        InitState init = underTest.init(initContext);
        assertThat(init).isEqualTo(InitState.INITIALIZED);
    }

    @Test
    public void testInitGetJaxRsClasses() {
        givenConfiguration();
        givenSpecifications(
                new Pair<>(JaxRsResourcePredicate.INSTANCE, MyResource.class),
                new Pair<>(JaxRsProviderPredicate.INSTANCE, MyProvider.class)
        );

        underTest.provideContainerContext(SeedRuntime.builder()
                .configuration(Coffig.builder().withProviders(new PrioritizedProvider()).build())
                .diagnosticManager(new DiagnosticManagerImpl())
                .context(seedRuntime.contextAs(ServletContext.class))
                .build());
        underTest.init(initContext);

        Collection<Class<?>> actualResources = Deencapsulation.getField(underTest, "resources");
        assertThat(actualResources).containsOnly(MyResource.class);
        Collection<Class<?>> actualProviders = Deencapsulation.getField(underTest, "providers");
        assertThat(actualProviders).contains(MyProvider.class);
    }

    @SafeVarargs
    private final void givenSpecifications(Pair<Predicate<Class<?>>, Class<?>>... specEntries) {
        final Map<Predicate<Class<?>>, Collection<Class<?>>> specsMap = new HashMap<>();
        for (Pair<Predicate<Class<?>>, Class<?>> specEntry : specEntries) {
            specsMap.put(specEntry.getValue0(), Lists.newArrayList(specEntry.getValue1()));
        }
        new Expectations() {{
            initContext.scannedTypesByPredicate();
            result = specsMap;
        }};
    }

    private void givenConfiguration() {
        new Expectations() {
            {
                initContext.dependency(ApplicationProvider.class);
                result = applicationProvider;
                applicationProvider.getApplication();
                result = application;
                application.getConfiguration();
                result = coffig;
                coffig.getProvider();
                times = -1;
                result = new CompositeProvider(new PrioritizedProvider());
                seedRuntime.contextAs(ServletContext.class);
                result = servletContext;
            }
        };
    }

    @Ignore
    @Path("/")
    private static class MyResource {
    }

    @Ignore
    @Provider
    private static class MyProvider {
    }

    static class Pair<T1, T2> {
        private final T1 t1;
        private final T2 t2;

        Pair(T1 t1, T2 t2) {
            this.t1 = t1;
            this.t2 = t2;
        }

        T1 getValue0() {
            return t1;
        }

        T2 getValue1() {
            return t2;
        }
    }
}
