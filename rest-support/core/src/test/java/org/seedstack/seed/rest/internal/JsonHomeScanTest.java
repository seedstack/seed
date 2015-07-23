/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.annotations.Ignore;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.rest.api.Rel;
import org.seedstack.seed.rest.internal.jsonhome.Resource;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Tests that JAX-RS resources annotated with @Rel on method or class are
 * properly scanned.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@Ignore // Tells nuun to not scan the test class
public class JsonHomeScanTest {

    private static final String REST_PATH = "/";
    private static final String BASE_REL = "http://example.org/rel/";
    private static final String BASE_PARAM = "http://example.org/param/";

    @Ignore
    static class MethodResource {

        @Rel(value = "/widgets/", expose = true)
        @Path("/widgets")
        @POST
        public Response post() {
            return null;
        }
    }

    @Ignore
    @Path("/catalog1")
    static class ClassResource {

        @Rel(value = "catalog1", expose = true)
        @POST
        public Response post() {
            return null;
        }
    }

    @Ignore
    @Rel(value = "shouldNotBeTakenInAccount")
    @Path("/catalog2/")
    static class ClassAndMethodResource {

        @Rel(value = "catalog2", expose = true)
        @Path("/widgets/")
        @POST
        public Response post() {
            return null;
        }
    }

    @Ignore
    @Path("/catalog3/")
    static class ClassAndMethodResource2 {

        @Rel(value = "catalog3", expose = true)
        @Path("/widgets/")
        @POST
        public Response post() {
            return null;
        }
    }

    @Ignore
    @Rel(value = "catalog4", expose = true)
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

        @Rel(value = "", expose = true)
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

    @Rel(value = "widget", expose = true)
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

    private Map<String, Resource> resourceMap;

    @Before
    public void before() {
        ResourceScanner resourceScanner = new ResourceScanner(REST_PATH, BASE_REL, BASE_PARAM);
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
        Assertions.assertThat(widgetResource.href()).isEqualTo("/widgets");

        // Path on class
        Resource catalogResource = resourceMap.get(UriBuilder.uri(BASE_REL, "catalog1"));
        Assertions.assertThat(catalogResource).isNotNull();
        Assertions.assertThat(catalogResource.rel()).isEqualTo(UriBuilder.uri(BASE_REL, "catalog1"));
        Assertions.assertThat(catalogResource.href()).isEqualTo("/catalog1");

        // Path on method and class (with two rel)
        Resource catalogWidgetResource = resourceMap.get(UriBuilder.uri(BASE_REL, "catalog2"));
        Assertions.assertThat(catalogWidgetResource).isNotNull();
        Assertions.assertThat(catalogWidgetResource.rel()).isEqualTo(UriBuilder.uri(BASE_REL, "catalog2"));
        Assertions.assertThat(catalogWidgetResource.href()).isEqualTo("/catalog2/widgets");

        // Path on method and class
        Resource catalogWidgetResource2 = resourceMap.get(UriBuilder.uri(BASE_REL, "catalog3"));
        Assertions.assertThat(catalogWidgetResource2).isNotNull();
        Assertions.assertThat(catalogWidgetResource2.rel()).isEqualTo(UriBuilder.uri(BASE_REL, "catalog3"));
        Assertions.assertThat(catalogWidgetResource2.href()).isEqualTo("/catalog3/widgets");

        // Path on method and class
        Resource catalogResource4 = resourceMap.get(UriBuilder.uri(BASE_REL, "catalog4"));
        Assertions.assertThat(catalogResource4).isNull();

        Resource unexposed = resourceMap.get(UriBuilder.uri(BASE_REL, "unexposed"));
        Assertions.assertThat(unexposed).isNull();
    }

    @Test(expected = Exception.class)
    public void test_bad_rel() {
        ResourceScanner resourceScanner = new ResourceScanner(REST_PATH, BASE_REL, BASE_PARAM);
        resourceScanner.scan(Lists.newArrayList(
                FakeResource.class,
                FakeResource2.class
        ));
        resourceMap = resourceScanner.jsonHomeResources();
    }
}
