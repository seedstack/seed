/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web;

import io.restassured.RestAssured;
import java.net.URL;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WebResourcesIT {
    @ArquillianResource
    private URL baseUrl;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @BeforeClass
    public static void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    @RunAsClient
    public void classpathWebResourcesWithDefaultConfigurationAreGzippedAndMinified() {
        RestAssured.expect().statusCode(200).header("Content-Encoding", Matchers.equalTo("gzip")).body(
                Matchers.containsString("var minifiedJS = {};")).when().get(baseUrl + "test.js");
    }

    @Test
    @RunAsClient
    public void classpathWebResourcesWithDefaultConfigurationAreGzippedOnTheFly() {
        RestAssured.expect().statusCode(200).header("Content-Encoding", Matchers.equalTo("gzip")).body(
                Matchers.containsString("var JS2 = {};")).when().get(baseUrl + "test2.js");
    }

    @Test
    @RunAsClient
    public void nonExistentResourceIs404() {
        RestAssured.expect().statusCode(404).when().get(baseUrl + "non-existent-resource");
    }

    @Test
    @RunAsClient
    public void notPregzippedResourceIsGzippedOnTheFlyTwice() {
        RestAssured.expect().statusCode(200).header("Content-Encoding", Matchers.equalTo("gzip")).body(
                Matchers.containsString("var JS2 = {};")).when().get(baseUrl + "test2.js");

        RestAssured.expect().statusCode(200).header("Content-Encoding", Matchers.equalTo("gzip")).body(
                Matchers.containsString("var JS2 = {};")).when().get(baseUrl + "test2.js");
    }
}
