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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CorsIT {
    private static final String WWW_OTHER_COM = "www.other.com";
    @ArquillianResource
    private URL baseUrl;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Test
    @RunAsClient
    public void corsSupportIsEnabledForGet() {
        RestAssured.given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "GET")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseUrl + "cors");
    }

    @Test
    @RunAsClient
    public void corsSupportIsEnabledForPost() {
        RestAssured.given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "POST")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseUrl + "cors");
    }

    @Test
    @RunAsClient
    public void corsSupportIsEnabledForPut() {
        RestAssured.given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "PUT")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseUrl + "cors");
    }

    @Test
    @RunAsClient
    public void corsSupportIsEnabledForDelete() {
        RestAssured.given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "DELETE")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseUrl + "cors");
    }

    @Test
    @RunAsClient
    public void corsSupportIsEnabledForHead() {
        RestAssured.given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "HEAD")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseUrl + "cors");
    }

    @Test
    @RunAsClient
    public void corsSupportIsEnabledForOptions() {
        RestAssured.given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "OPTIONS")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseUrl + "cors");
    }

    @Test
    @RunAsClient
    public void traceRequestShouldBeRefused() {
        RestAssured.given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "TRACE")
                .expect()
                .statusCode(405)
                .when()
                .options(baseUrl + "cors");
    }
}
