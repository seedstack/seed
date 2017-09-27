/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper;
import com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kametic.specifications.Specification;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.Application;
import org.seedstack.seed.Ignore;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.configuration.PrioritizedProvider;
import org.seedstack.seed.core.internal.diagnostic.DiagnosticManagerImpl;
import org.seedstack.seed.spi.ApplicationProvider;

@RunWith(JMockit.class)
public class RestPluginTest {
    private RestPlugin underTest = new RestPlugin();
    @Mocked
    private InitContext initContext;
    @Mocked
    private ApplicationProvider applicationProvider;
    @Mocked
    private Application application;
    @Mocked
    private ServletContext servletContext;

    @Test
    public void testName() throws Exception {
        assertThat(underTest.name()).isEqualTo("rest");
    }

    @Test
    public void testRequestScanForJaxRsClasses() throws Exception {
        assertScanSpecification(JaxRsProviderSpecification.INSTANCE);
        assertScanSpecification(JaxRsResourceSpecification.INSTANCE);
    }

    private void assertScanSpecification(Specification<Class<?>> specification) {
        boolean scanSpecification = false;
        for (ClasspathScanRequest classpathScanRequest : underTest.classpathScanRequests()) {
            if (classpathScanRequest.specification == specification) {
                scanSpecification = true;
                break;
            }
        }
        assertThat(scanSpecification).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testWithoutServletContext() throws Exception {
        givenConfiguration();
        givenSpecifications(
                new Pair(JaxRsResourceSpecification.INSTANCE, MyResource.class),
                new Pair(JaxRsProviderSpecification.INSTANCE, MyProvider.class)
        );
        InitState init = underTest.init(initContext);
        assertThat(init).isEqualTo(InitState.INITIALIZED);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInitGetJaxRsClasses() throws Exception {
        givenConfiguration();
        givenSpecifications(
                new Pair(JaxRsResourceSpecification.INSTANCE, MyResource.class),
                new Pair(JaxRsProviderSpecification.INSTANCE, MyProvider.class)
        );

        underTest.provideContainerContext(SeedRuntime.builder()
                .configuration(Coffig.builder().withProviders(new PrioritizedProvider()).build())
                .diagnosticManager(new DiagnosticManagerImpl())
                .context(servletContext)
                .build());
        underTest.init(initContext);

        Collection<Class<?>> actualResources = Deencapsulation.getField(underTest, "resources");
        assertThat(actualResources).containsOnly(MyResource.class);
        Collection<Class<?>> actualProviders = Deencapsulation.getField(underTest, "providers");
        assertThat(actualProviders).containsOnly(
                MyProvider.class,
                JsonMappingExceptionMapper.class,
                JsonParseExceptionMapper.class,
                JacksonJsonProvider.class,
                JacksonJaxbJsonProvider.class
        );
    }

    private void givenSpecifications(Pair<Specification<Class<?>>, Class<?>>... specEntries) {
        final Map<Specification, Collection<Class<?>>> specsMap = new HashMap<>();
        for (Pair<Specification<Class<?>>, Class<?>> specEntry : specEntries) {
            specsMap.put(specEntry.getValue0(), Lists.newArrayList(specEntry.getValue1()));
        }
        new NonStrictExpectations() {{
            initContext.scannedTypesBySpecification();
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
                result = Coffig.builder().build();
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

    public class Pair<T1, T2> {
        private final T1 t1;
        private final T2 t2;

        public Pair(T1 t1, T2 t2) {
            this.t1 = t1;
            this.t2 = t2;
        }

        public T1 getValue0() {
            return t1;
        }

        public T2 getValue1() {
            return t2;
        }
    }
}
