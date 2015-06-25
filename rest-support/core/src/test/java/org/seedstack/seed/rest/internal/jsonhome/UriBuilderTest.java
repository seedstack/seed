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

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class UriBuilderTest {

    @Test
    public void testBuildPath() {
        Assertions.assertThat(UriBuilder.path("/base", "/foo", "bar")).isEqualTo("/base/foo/bar");
        Assertions.assertThat(UriBuilder.path("/base/", "/foo/", "bar")).isEqualTo("/base/foo/bar");
        Assertions.assertThat(UriBuilder.path("http://base/", "/foo/", "/bar/")).isEqualTo("http://base/foo/bar");
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

        hrefTemplate = UriBuilder.stripJaxRsRegex("/widgets/{widgetName: [a-zA-Z][a-zA-Z_0-9]}/items/{itemId: [a-zA-Z][a-zA-Z_0-9]}");
        Assertions.assertThat(hrefTemplate).isEqualTo("/widgets/{widgetName}/items/{itemId}");

        hrefTemplate = UriBuilder.stripJaxRsRegex("/widgets/{widgetName: [a-zA-Z][a-zA-Z_0-9]}/items/{itemId: [a-zA-Z][a-zA-Z_0-9]}/id");
        Assertions.assertThat(hrefTemplate).isEqualTo("/widgets/{widgetName}/items/{itemId}/id");
    }
}
