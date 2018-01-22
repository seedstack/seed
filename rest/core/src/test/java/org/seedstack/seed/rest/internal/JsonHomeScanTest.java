/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.annotations.Ignore;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.rest.Rel;
import org.seedstack.seed.rest.RestConfig;
import org.seedstack.seed.rest.internal.jsonhome.Resource;

/**
 * Tests that JAX-RS resources annotated with @Rel on method or class are
 * properly scanned.
 */
@Ignore // Tells nuun to not scan the test class
@RunWith(JMockit.class)
public class JsonHomeScanTest {

    private static final String SERVLET_CONTEXT_PATH = "/app/";
    private static final String REST_PATH = "/rest/";
    private static final String BASE_REL = "http://example.org/rel/";
    private static final String BASE_PARAM = "http://example.org/param/";
    @Mocked
    private RestConfig restConfig;
    @Mocked
    private ServletContext servletContext;
    private Map<String, Resource> resourceMap;

    @Before
    public void before() {
        new Expectations() {
            {
                restConfig.getPath();
                result = REST_PATH;
                restConfig.getBaseRel();
                result = BASE_REL;
                restConfig.getBaseParam();
                result = BASE_PARAM;
                servletContext.getContextPath();
                result = SERVLET_CONTEXT_PATH;
            }
        };

        ResourceScanner resourceScanner = new ResourceScanner(restConfig, servletContext);
        resourceScanner.scan(Lists.newArrayList(
                MethodResource.class,
                ClassResource.class,
                ClassAndMethodResource.class,
                ClassAndMethodResource2.class,
                ClassAndMethodResource3.class
        ));
        resourceMap = resourceScanner.jsonHomeResources();
        Assertions.assertThat(resourceMap).isNotNull();
    }

    @Test
    public void scan_json_home_resources() {
        // Path on method
        Resource widgetResource = resourceMap.get(UriBuilder.uri(BASE_REL, "widgets"));
        Assertions.assertThat(widgetResource).isNotNull();
        Assertions.assertThat(widgetResource.rel()).isEqualTo(UriBuilder.uri(BASE_REL, "widgets"));
        Assertions.assertThat(widgetResource.href()).isEqualTo("/app/rest/widgets");

        // Path on class
        Resource catalogResource = resourceMap.get(UriBuilder.uri(BASE_REL, "catalog1"));
        Assertions.assertThat(catalogResource).isNotNull();
        Assertions.assertThat(catalogResource.rel()).isEqualTo(UriBuilder.uri(BASE_REL, "catalog1"));
        Assertions.assertThat(catalogResource.href()).isEqualTo("/app/rest/catalog1");

        // Path on method and class (with two rel)
        Resource catalogWidgetResource = resourceMap.get(UriBuilder.uri(BASE_REL, "catalog2"));
        Assertions.assertThat(catalogWidgetResource).isNotNull();
        Assertions.assertThat(catalogWidgetResource.rel()).isEqualTo(UriBuilder.uri(BASE_REL, "catalog2"));
        Assertions.assertThat(catalogWidgetResource.href()).isEqualTo("/app/rest/catalog2/widgets");

        // Path on method and class
        Resource catalogWidgetResource2 = resourceMap.get(UriBuilder.uri(BASE_REL, "catalog3"));
        Assertions.assertThat(catalogWidgetResource2).isNotNull();
        Assertions.assertThat(catalogWidgetResource2.rel()).isEqualTo(UriBuilder.uri(BASE_REL, "catalog3"));
        Assertions.assertThat(catalogWidgetResource2.href()).isEqualTo("/app/rest/catalog3/widgets");

        // Path on method and class
        Resource catalogResource4 = resourceMap.get(UriBuilder.uri(BASE_REL, "catalog4"));
        Assertions.assertThat(catalogResource4).isNull();

        Resource unexposed = resourceMap.get(UriBuilder.uri(BASE_REL, "unexposed"));
        Assertions.assertThat(unexposed).isNull();
    }

    @Test(expected = Exception.class)
    public void test_bad_rel() {
        ResourceScanner resourceScanner = new ResourceScanner(restConfig, servletContext);
        resourceScanner.scan(Lists.newArrayList(
                FakeResource.class,
                FakeResource2.class
        ));
        resourceMap = resourceScanner.jsonHomeResources();
    }

    @Ignore
    static class MethodResource {

        @Rel(value = "/widgets/", home = true)
        @Path("/widgets")
        @POST
        public Response post() {
            return null;
        }
    }

    @Ignore
    @Path("/catalog1")
    static class ClassResource {

        @Rel(value = "catalog1", home = true)
        @POST
        public Response post() {
            return null;
        }
    }

    @Ignore
    @Rel(value = "shouldNotBeTakenInAccount")
    @Path("/catalog2/")
    static class ClassAndMethodResource {

        @Rel(value = "catalog2", home = true)
        @Path("/widgets/")
        @POST
        public Response post() {
            return null;
        }
    }

    @Ignore
    @Path("/catalog3/")
    static class ClassAndMethodResource2 {

        @Rel(value = "catalog3", home = true)
        @Path("/widgets/")
        @POST
        public Response post() {
            return null;
        }
    }

    @Ignore
    @Rel(value = "catalog4", home = true)
    @Path("/catalog4/")
    static class ClassAndMethodResource3 {

        @Rel("unexposed")
        @Path("/widgets/")
        @POST
        public Response post() {
            return null;
        }
    }

    @Ignore
    static class FakeResource {

        @Rel(value = "", home = true)
        @POST
        public Response post() {
            return null;
        }
    }

    @Ignore
    static class FakeResource2 {

        @Path("fake")
        @POST
        public Response post() {
            return null;
        }
    }

    @Rel(value = "widget", home = true)
    @Path("/widgets/{widgetName: [a-zA-Z][a-zA-Z_0-9]}")
    static class MyLinkTemplateResource {

        @GET
        public Response get(@PathParam("widgetName") String widgetId, @QueryParam("pageSize") Integer pageSize) {
            return null;
        }

        @PUT
        public Response put(@PathParam("widgetName") String widgetId) {
            return null;
        }
    }
}
