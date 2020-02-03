/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.jsonhome;

import java.util.HashMap;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.SeedException;

public class ResourceTest {

    public static final String REL = "http://example.org/rel/catalog";
    public static final String HREF = "http://example.org/catalog";
    private Resource resource;
    private Hints hints;

    @Before
    public void setup() {
        hints = new Hints();
        hints.format("application/json", null);
        hints.format("application/xml", null);
        hints.addAllow("POST");
        resource = new Resource(REL, HREF, hints);
    }

    @Test
    public void merge_formats() {
        Hints newHints = new Hints();
        newHints.format("application/json", null);
        newHints.format("application/hal+json", null);
        Resource newResource = new Resource(REL, HREF, newHints);

        Assertions.assertThat(resource.hints().getFormats()).hasSize(2);

        resource.merge(newResource);

        Assertions.assertThat(resource.hints().getFormats()).hasSize(3);
    }

    @Test
    public void merge_allow() {
        Hints newHints = new Hints();
        newHints.addAllow("POST");
        Resource newResource = new Resource(REL, HREF, newHints);

        Assertions.assertThat(resource.hints().getAllow()).hasSize(1);

        resource.merge(newResource);

        Assertions.assertThat(resource.hints().getAllow()).hasSize(2);
        Assertions.assertThat(resource.hints().getAllow().contains("POST")).isTrue();
    }

    @Test(expected = SeedException.class)
    public void cannot_merge_different_resources() {
        Resource newResource = new Resource("http://example.org/rel/anotherResource", HREF, hints);
        resource.merge(newResource);
    }

    @Test(expected = SeedException.class)
    public void resources_cannot_have_different_href() {
        Resource newResource = new Resource(REL, "http://example.org/catalogs", hints);
        resource.merge(newResource);
    }

    @Test(expected = SeedException.class)
    public void resources_cannot_have_different_href2() {
        Resource newResource = new Resource(REL, "http://example.org/catalogs/{id}", new HashMap<>(), new HashMap<>(),
                hints);
        resource.merge(newResource);
    }
}
