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
import org.assertj.core.api.Assertions;
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
import org.seedstack.seed.web.fixtures.servlet.TestFilter;
import org.seedstack.seed.web.fixtures.servlet.TestListener;
import org.seedstack.seed.web.fixtures.servlet.TestServlet;

@RunWith(Arquillian.class)
public class ServletIT {
    @ArquillianResource
    private URL baseUrl;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Test
    @RunAsClient
    public void listenerIsCorrectlyRegistered() {
        Assertions.assertThat(TestListener.hasBeenCalled()).isTrue();
    }

    @Test
    @RunAsClient
    public void servletIsCorrectlyRegistered() {
        RestAssured.expect()
                .statusCode(200)
                .body(Matchers.startsWith(TestServlet.CONTENT))
                .when()
                .get(baseUrl + "testServlet1");
        RestAssured.expect()
                .statusCode(200)
                .body(Matchers.startsWith(TestServlet.CONTENT))
                .when()
                .get(baseUrl + "testServlet2");
    }

    @Test
    @RunAsClient
    public void servletParamsAreCorrectlyInitialized() {
        RestAssured.expect()
                .statusCode(200)
                .body(Matchers.endsWith(TestServlet.PARAM1_VALUE))
                .when()
                .get(baseUrl + "testServlet1");
        RestAssured.expect()
                .statusCode(200)
                .body(Matchers.endsWith(TestServlet.PARAM1_VALUE))
                .when()
                .get(baseUrl + "testServlet2");
    }

    @Test
    @RunAsClient
    public void filterIsCorrectlyRegistered() {
        RestAssured.expect()
                .statusCode(200)
                .body(Matchers.startsWith(TestFilter.CONTENT))
                .when()
                .get(baseUrl + "testFilter1");
        RestAssured.expect()
                .statusCode(200)
                .body(Matchers.startsWith(TestFilter.CONTENT))
                .when()
                .get(baseUrl + "testFilter2");
    }

    @Test
    @RunAsClient
    public void filterParamsAreCorrectlyInitialized() {
        RestAssured.expect()
                .statusCode(200)
                .body(Matchers.endsWith(TestFilter.PARAM1_VALUE))
                .when()
                .get(baseUrl + "testFilter1");
        RestAssured.expect()
                .statusCode(200)
                .body(Matchers.endsWith(TestFilter.PARAM1_VALUE))
                .when()
                .get(baseUrl + "testFilter1");
    }
}
