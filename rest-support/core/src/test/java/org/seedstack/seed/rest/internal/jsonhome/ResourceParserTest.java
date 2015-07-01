/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.jsonhome;

import io.nuun.kernel.api.annotations.Ignore;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.rest.api.Rel;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@Ignore // Tells nuun to not scan the test class
public class ResourceParserTest {

    private static final String BASE_REL = "http://example.org/rel/";
    private static final String BASE_PARAM = "http://example.org/param/";
    private ResourceParser underTest;

    @Before
    public void before() {
        underTest = new ResourceParser(BASE_REL, BASE_PARAM);
    }

    @Ignore
    static class MethodResource {

        @Rel(value = "/widgets/", expose = true)
        @Path("/widgets")
        public Response post() { return null; }
    }

    @Ignore
    @Path("/catalog1")
    static class ClassResource {

        @Rel(value = "catalog1", expose = true)
        public Response post() { return null; }
    }

    @Ignore
    @Rel(value = "catalog2")
    @Path("/catalog2/")
    static class ClassAndMethodResource {

        @Rel(value = "widgets", expose = true)
        @Path("/widgets/")
        public Response post() { return null; }
    }

    @Ignore
    @Path("/catalog3/")
    static class ClassAndMethodResource2 {

        @Rel(value = "widgets", expose = true)
        @Path("/widgets/")
        public Response post() { return null; }
    }

    @Ignore
    @Rel(value = "bla", expose = true)
    @Path("/catalog4/")
    static class ClassAndMethodResource3 {

        @Rel("widgets")
        @Path("/widgets/")
        public Response post() { return null; }
    }

    @Ignore
    static class FakeResource {

        @Rel(value = "", expose = true)
        public Response post() { return null; }
    }

    @Ignore
    static class FakeResource2 {

        @Path("fake")
        public Response post() { return null; }
    }

    @Test
    public void testCreateDirectLinkResource() {
        // Path on method
        Resource widgetResource = underTest.parse(method(MethodResource.class, "post"));
        Assertions.assertThat(widgetResource.rel()).isEqualTo(UriBuilder.path(BASE_REL, "widgets"));
        Assertions.assertThat(widgetResource.href()).isEqualTo("/widgets");

        // Path on class
        Resource catalogResource = underTest.parse(method(ClassResource.class, "post"));
        Assertions.assertThat(catalogResource.rel()).isEqualTo(UriBuilder.path(BASE_REL, "catalog1"));
        Assertions.assertThat(catalogResource.href()).isEqualTo("/catalog1");

        // Path on method and class (with two rel)
        Resource catalogWidgetResource = underTest.parse(method(ClassAndMethodResource.class, "post"));
        Assertions.assertThat(catalogWidgetResource.rel()).isEqualTo(UriBuilder.path(BASE_REL, "widgets"));
        Assertions.assertThat(catalogWidgetResource.href()).isEqualTo("/catalog2/widgets");

        // Path on method and class
        Resource catalogWidgetResource2 = underTest.parse(method(ClassAndMethodResource2.class, "post"));
        Assertions.assertThat(catalogWidgetResource2.rel()).isEqualTo(UriBuilder.path(BASE_REL, "widgets"));
        Assertions.assertThat(catalogWidgetResource2.href()).isEqualTo("/catalog3/widgets");

        // Path on method and class
        Resource catalogWidgetResource3 = underTest.parse(method(ClassAndMethodResource3.class, "post"));
        Assertions.assertThat(catalogWidgetResource3).isNull();
    }

    @Test
    public void testRelDetection() {
        Assertions.assertThat(underTest.findRel(method(MethodResource.class, "post"))).isEqualTo(UriBuilder.path(BASE_REL, "widgets"));
        Assertions.assertThat(underTest.findRel(method(ClassAndMethodResource.class, "post"))).isEqualTo(UriBuilder.path(BASE_REL, "widgets"));
        Assertions.assertThat(underTest.findRel(method(MyLinkTemplateResource.class, "get", String.class, Integer.class))).isEqualTo(UriBuilder.path(BASE_REL, "widget"));
    }

    @Rel(value = "widget", expose = true)
    @Path("/widgets/{widgetName: [a-zA-Z][a-zA-Z_0-9]}")
    static class MyLinkTemplateResource {

        @GET
        public Response get(@PathParam("widgetName") String widgetId, @QueryParam("pageSize") Integer pageSize) { return null; }

        @PUT
        public Response put(@PathParam("widgetName") String widgetId) { return null; }
    }


    @Rel(value = "widgets", expose = true)
    @Path("/widgets/")
    static class MyLinkTemplateResource2 {

        @GET
        public Response get(@QueryParam("pageSize") Integer pageSize, @QueryParam("pageNumber") Integer pageNumber) { return null; }
    }

    @Test
    public void testCreateLinkTemplateResource() {
        // Path on method
        Resource linkTemplateResource = underTest.parse(method(MyLinkTemplateResource.class, "get", String.class, Integer.class));
        Assertions.assertThat(linkTemplateResource).isNotNull();
        Assertions.assertThat(linkTemplateResource.rel()).isEqualTo(UriBuilder.path(BASE_REL, "widget"));

        Assertions.assertThat(linkTemplateResource.hrefTemplate()).isEqualTo("/widgets/{widgetName}");
        Assertions.assertThat(linkTemplateResource.hrefVars()).hasSize(2);
        Assertions.assertThat(linkTemplateResource.hrefVars().get("widgetName")).isEqualTo(UriBuilder.path(BASE_PARAM, "widgetName"));
        Assertions.assertThat(linkTemplateResource.hrefVars().get("pageSize")).isEqualTo(UriBuilder.path(BASE_PARAM, "pageSize"));
    }

    @Test
    public void testCreateResource() {
        Map<String, Resource> resources = new ResourceParser(BASE_REL, BASE_PARAM).parse(MyLinkTemplateResource.class);

        Assertions.assertThat(resources).hasSize(1);

        Resource widgetResource = resources.get(UriBuilder.path(BASE_REL, "/widget"));
        Assertions.assertThat(widgetResource).isNotNull();

        Map<String, Object> representation = widgetResource.toRepresentation();
        Assertions.assertThat(representation).hasSize(3);

        String hrefTemplate = (String) representation.get("href-template");
        Assertions.assertThat(hrefTemplate).isEqualTo("/widgets/{widgetName}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateResourceWithNoPath() {
        underTest.parse(method(FakeResource.class, "post"));
    }

    @Test
    public void testCreateResourceWithNoRel() {
        Resource resource = underTest.parse(method(FakeResource2.class, "post"));
        Assertions.assertThat(resource).isNull();
    }

    private Method method(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            return clazz.getMethod(methodName, params);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
