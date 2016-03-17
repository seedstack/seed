/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper;
import com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.apache.commons.configuration.Configuration;
import org.javatuples.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kametic.specifications.Specification;
import org.seedstack.seed.Ignore;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.seedstack.seed.rest.internal.RestPlugin.providersSpecification;
import static org.seedstack.seed.rest.internal.RestPlugin.resourcesSpecification;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@RunWith(JMockit.class)
public class RestPluginTest {

    private RestPlugin underTest = new RestPlugin();
    @Mocked
    private InitContext initContext;
    @Mocked
    private Configuration configuration;
    @Mocked
    private ConfigurationProvider configurationProvider;
    @Mocked
    private ServletContext servletContext;

    @Test
    public void testName() throws Exception {
        assertThat(underTest.name()).isEqualTo("rest");
    }

    @Test
    public void testRequestScanForJaxRsClasses() throws Exception {
        assertScanSpecification(providersSpecification);
        assertScanSpecification(RestPlugin.resourcesSpecification);
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
                new Pair(resourcesSpecification, MyResource.class),
                new Pair(providersSpecification, MyProvider.class)
        );
        InitState init = underTest.init(initContext);
        assertThat(init).isEqualTo(InitState.INITIALIZED);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInitGetJaxRsClasses() throws Exception {
        givenConfiguration();
        givenSpecifications(
                new Pair(resourcesSpecification, MyResource.class),
                new Pair(providersSpecification, MyProvider.class)
        );

        underTest.provideContainerContext(servletContext);
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

    @Ignore
    @Path("/")
    private static class MyResource {
    }

    @Ignore
    @Provider
    private static class MyProvider {
    }

    private void givenSpecifications(Pair<Specification<Class<?>>, Class<?>>... specEntries) {
        final Map<Specification, Collection<Class<?>>> specsMap = new HashMap<Specification, Collection<Class<?>>>();
        for (Pair<Specification<Class<?>>, Class<?>> specEntry : specEntries) {
            specsMap.put(specEntry.getValue0(), Lists.<Class<?>>newArrayList(specEntry.getValue1()));
        }
        new Expectations() {{
            initContext.scannedTypesBySpecification();
            result = specsMap;
        }};
    }

    private void givenConfiguration() {
        new Expectations() {
            {
                initContext.dependency(ConfigurationProvider.class);
                result = configurationProvider;
                configurationProvider.getConfiguration();
                result = configuration;
            }
        };
    }
}
