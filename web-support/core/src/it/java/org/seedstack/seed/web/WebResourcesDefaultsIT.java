/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web;

import org.seedstack.seed.it.AbstractSeedWebIT;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.net.URL;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class WebResourcesDefaultsIT extends AbstractSeedWebIT {
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addAsWebResource("META-INF/resources/test.js", "/docroot-test.js").addAsWebResource("META-INF/resources/test.js.gz", "/docroot-test.js.gz").addAsWebResource("META-INF/resources/test.min.js", "/docroot-test.min.js").addAsWebResource("META-INF/resources/test.min.js.gz", "/docroot-test.min.js.gz").addAsWebResource("META-INF/resources/test2.js", "/docroot-test2.js").setWebXML("WEB-INF/web.xml");
    }

    @Test
    @RunAsClient
    public void classpath_webresources_with_default_configuration_are_gzipped_and_minified(@ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(200).header("Content-Encoding", equalTo("gzip")).body(containsString("var minifiedJS = {};")).when().get(baseURL.toString() + "test.js");
    }

    @Test
    @RunAsClient
    public void docroot_webresources_with_default_configuration_are_gzipped_and_minified(@ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(200).header("Content-Encoding", equalTo("gzip")).body(containsString("var minifiedJS = {};")).when().get(baseURL.toString() + "docroot-test.js");
    }

    @Test
    @RunAsClient
    public void classpath_webresources_with_default_configuration_are_gzipped_on_the_fly(@ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(200).header("Content-Encoding", equalTo("gzip")).body(containsString("var JS2 = {};")).when().get(baseURL.toString() + "test2.js");
    }

    @Test
    @RunAsClient
    public void docroot_webresources_with_default_configuration_are_gzipped_on_the_fly(@ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(200).header("Content-Encoding", equalTo("gzip")).body(containsString("var JS2 = {};")).when().get(baseURL.toString() + "docroot-test2.js");
    }

    @Test
    @RunAsClient
    public void non_existent_resource_is_404(@ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(404).when().get(baseURL.toString() + "resources/non-existent-resource");
    }

    @Test
    @RunAsClient
    public void not_pregzipped_resource_is_gzipped_on_the_fly_twice(@ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(200).header("Content-Encoding", equalTo("gzip")).body(containsString("var JS2 = {};")).when().get(baseURL.toString() + "docroot-test2.js");

        expect().statusCode(200).header("Content-Encoding", equalTo("gzip")).body(containsString("var JS2 = {};")).when().get(baseURL.toString() + "docroot-test2.js");
    }
}
