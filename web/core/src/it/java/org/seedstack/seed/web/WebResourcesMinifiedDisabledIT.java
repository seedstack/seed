/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedWebIT;

public class WebResourcesMinifiedDisabledIT extends AbstractSeedWebIT {
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(WebArchive.class)
                .addAsResource("configuration/minified-disabled.yaml", "META-INF/configuration/minified-disabled.yaml")
                .addAsWebResource("META-INF/resources/resources/test.js", "/resources/docroot-test.js")
                .addAsWebResource("META-INF/resources/resources/test.js.gz", "/resources/docroot-test.js.gz")
                .addAsWebResource("META-INF/resources/resources/test.min.js", "/resources/docroot-test.min.js")
                .addAsWebResource("META-INF/resources/resources/test.min.js.gz", "/resources/docroot-test.min.js.gz");
    }

    @Test
    @RunAsClient
    public void classpath_webresources_with_minified_disabled_are_gzipped_but_not_minified(
            @ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(200).header("Content-Encoding", equalTo("gzip")).body(
                containsString("var JS = {};")).when().get(baseURL.toString() + "resources/test.js");
    }

    @Test
    @RunAsClient
    public void docroot_webresources_with_minified_disabled_are_gzipped_but_not_minified(
            @ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(200).header("Content-Encoding", equalTo("gzip")).body(
                containsString("var JS = {};")).when().get(baseURL.toString() + "resources/docroot-test.js");
    }
}
