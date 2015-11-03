/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
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
import org.seedstack.seed.rest.Rel;
import org.seedstack.seed.rest.hal.Link;
import org.seedstack.seed.rest.internal.jsonhome.Resource;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@Ignore // Tells nuun to not scan the test class
public class ResourceParserTest {

    private static final String REST_PATH = "/rest/";
    private static final String BASE_REL = "http://example.org/rel/";
    private static final String BASE_PARAM = "http://example.org/param/";

    private Map<String, Resource> resourceMap;
    private Map<String, Link> links;

    @Ignore
    @Rel(value = "widget", home = true)
    @Path("widgets/{widgetName: [a-zA-Z][a-zA-Z_0-9]}")
    private static class MyLinkTemplateResource {

        @GET
        public Response get(@PathParam("widgetName") String widgetId, @QueryParam("lang") String lang) { return null; }

        @PUT
        public Response put(@PathParam("widgetName") String widgetId) { return null; }
    }

    @Before
    public void before() {
        ResourceScanner resourceScanner = new ResourceScanner(REST_PATH, BASE_REL, BASE_PARAM);
        resourceScanner.scan(Lists.<Class<?>>newArrayList(MyLinkTemplateResource.class));
        resourceMap = resourceScanner.jsonHomeResources();
        links = resourceScanner.halLinks();
        Assertions.assertThat(resourceMap).isNotNull();
    }

    @Test
    public void testCreateLinkTemplateResource() {
        // Path on method
        Resource underTest = resourceMap.get(UriBuilder.uri(BASE_REL, "widget"));
        Assertions.assertThat(underTest).isNotNull();
        Assertions.assertThat(underTest.rel()).isEqualTo(UriBuilder.uri(BASE_REL, "widget"));

        Assertions.assertThat(underTest.hrefTemplate()).isEqualTo("/rest/widgets/{widgetName}{?lang}");
        Assertions.assertThat(underTest.hrefVars()).hasSize(2);
        Assertions.assertThat(underTest.hrefVars().get("widgetName")).isEqualTo(UriBuilder.uri(BASE_PARAM, "widgetName"));
        Assertions.assertThat(underTest.hrefVars().get("lang")).isEqualTo(UriBuilder.uri(BASE_PARAM, "lang"));
    }

    @Test
    public void testCreateResource() {
        Resource widgetResource = resourceMap.get(UriBuilder.uri(BASE_REL, "widget"));
        Assertions.assertThat(widgetResource).isNotNull();
        Map<String, Object> underTest = widgetResource.toRepresentation();

        Assertions.assertThat(underTest).hasSize(3);

        String hrefTemplate = (String) underTest.get("href-template");
        Assertions.assertThat(hrefTemplate).isEqualTo("/rest/widgets/{widgetName}");
    }

    @Test
    public void testHalLinks() {
        Link widgetLink = links.get("widget");
        Assertions.assertThat(widgetLink).isNotNull();
        Assertions.assertThat(widgetLink.getHref()).isEqualTo("/rest/widgets/{widgetName}{?lang}");
        Assertions.assertThat(widgetLink.set("lang", "EN").set("widgetName", 1).expand()).isEqualTo("/rest/widgets/1?lang=EN");
    }
}
