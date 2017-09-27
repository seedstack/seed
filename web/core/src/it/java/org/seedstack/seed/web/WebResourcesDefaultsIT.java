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

public class WebResourcesDefaultsIT extends AbstractSeedWebIT {
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(WebArchive.class)
                .addAsWebResource("META-INF/resources/resources/test.js", "/resources/docroot-test.js")
                .addAsWebResource("META-INF/resources/resources/test.js.gz", "/resources/docroot-test.js.gz")
                .addAsWebResource("META-INF/resources/resources/test.min.js", "/resources/docroot-test.min.js")
                .addAsWebResource("META-INF/resources/resources/test.min.js.gz", "/resources/docroot-test.min.js.gz")
                .addAsWebResource("META-INF/resources/resources/test2.js", "/resources/docroot-test2.js");
    }

    @Test
    @RunAsClient
    public void classpath_webresources_with_default_configuration_are_gzipped_and_minified(
            @ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(200).header("Content-Encoding", equalTo("gzip")).body(
                containsString("var minifiedJS = {};")).when().get(baseURL.toString() + "resources/test.js");
    }

    @Test
    @RunAsClient
    public void docroot_webresources_with_default_configuration_are_gzipped_and_minified(
            @ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(200).header("Content-Encoding", equalTo("gzip")).body(
                containsString("var minifiedJS = {};")).when().get(baseURL.toString() + "resources/docroot-test.js");
    }

    @Test
    @RunAsClient
    public void classpath_webresources_with_default_configuration_are_gzipped_on_the_fly(
            @ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(200).header("Content-Encoding", equalTo("gzip")).body(
                containsString("var JS2 = {};")).when().get(baseURL.toString() + "resources/test2.js");
    }

    @Test
    @RunAsClient
    public void docroot_webresources_with_default_configuration_are_gzipped_on_the_fly(
            @ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(200).header("Content-Encoding", equalTo("gzip")).body(
                containsString("var JS2 = {};")).when().get(baseURL.toString() + "resources/docroot-test2.js");
    }

    @Test
    @RunAsClient
    public void non_existent_resource_is_404(@ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(404).when().get(baseURL.toString() + "resources/non-existent-resource");
    }

    @Test
    @RunAsClient
    public void not_pregzipped_resource_is_gzipped_on_the_fly_twice(@ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(200).header("Content-Encoding", equalTo("gzip")).body(
                containsString("var JS2 = {};")).when().get(baseURL.toString() + "resources/docroot-test2.js");

        expect().statusCode(200).header("Content-Encoding", equalTo("gzip")).body(
                containsString("var JS2 = {};")).when().get(baseURL.toString() + "resources/docroot-test2.js");
    }
}
