/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class UriBuilderTest {

    @Test
    public void testBuildPath() {
        Assertions.assertThat(UriBuilder.uri("/base", "/foo", "bar")).isEqualTo("/base/foo/bar");
        Assertions.assertThat(UriBuilder.uri("/base/", "/foo/", "bar")).isEqualTo("/base/foo/bar");
        Assertions.assertThat(UriBuilder.uri("http://base/", "/foo/", "/bar/")).isEqualTo("http://base/foo/bar");
    }

    @Test
    public void testStripSlash() {
        Assertions.assertThat(UriBuilder.stripLeadingSlash("/plop/")).isEqualTo("/plop");
        Assertions.assertThat(UriBuilder.stripLeadingSlash("/plop")).isEqualTo("/plop");
        Assertions.assertThat(UriBuilder.stripLeadingSlash("plop")).isEqualTo("plop");
    }

    @Test
    public void testStripJaxRsRegex() {
        String hrefTemplate = UriBuilder.stripJaxRsRegex("{widgetName: [a-zA-Z][a-zA-Z_0-9]}");
        Assertions.assertThat(hrefTemplate).isEqualTo("{widgetName}");

        hrefTemplate = UriBuilder.stripJaxRsRegex(
                "/widgets/{widgetName: [a-zA-Z][a-zA-Z_0-9]}/items/{itemId: [a-zA-Z][a-zA-Z_0-9]}");
        Assertions.assertThat(hrefTemplate).isEqualTo("/widgets/{widgetName}/items/{itemId}");

        hrefTemplate = UriBuilder.stripJaxRsRegex(
                "/widgets/{widgetName: [a-zA-Z][a-zA-Z_0-9]}/items/{itemId: [a-zA-Z][a-zA-Z_0-9]}/id");
        Assertions.assertThat(hrefTemplate).isEqualTo("/widgets/{widgetName}/items/{itemId}/id");

        hrefTemplate = UriBuilder.stripJaxRsRegex("/widgets/{widgetName:.+}");
        Assertions.assertThat(hrefTemplate).isEqualTo("/widgets/{widgetName}");

        hrefTemplate = UriBuilder.stripJaxRsRegex("/widgets/{ widgetName : .+ }");
        Assertions.assertThat(hrefTemplate).isEqualTo("/widgets/{widgetName}");
    }
}
