/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web;

import static com.jayway.restassured.RestAssured.given;

import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedWebIT;

public class CorsIT extends AbstractSeedWebIT {
    private static final String WWW_OTHER_COM = "www.other.com";

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(WebArchive.class)
                .addAsResource("configuration/cors.yaml", "META-INF/configuration/cors.yaml");
    }

    @Test
    @RunAsClient
    public void cors_support_is_enabled_for_get(@ArquillianResource URL baseURL) throws Exception {
        given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "GET")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseURL.toString() + "cors");
    }

    @Test
    @RunAsClient
    public void cors_support_is_enabled_for_post(@ArquillianResource URL baseURL) throws Exception {
        given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "POST")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseURL.toString() + "cors");
    }

    @Test
    @RunAsClient
    public void cors_support_is_enabled_for_put(@ArquillianResource URL baseURL) throws Exception {
        given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "PUT")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseURL.toString() + "cors");
    }

    @Test
    @RunAsClient
    public void cors_support_is_enabled_for_delete(@ArquillianResource URL baseURL) throws Exception {
        given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "DELETE")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseURL.toString() + "cors");
    }

    @Test
    @RunAsClient
    public void cors_support_is_enabled_for_head(@ArquillianResource URL baseURL) throws Exception {
        given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "HEAD")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseURL.toString() + "cors");
    }

    @Test
    @RunAsClient
    public void cors_support_is_enabled_for_options(@ArquillianResource URL baseURL) throws Exception {
        given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "OPTIONS")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseURL.toString() + "cors");
    }

    @Test
    @RunAsClient
    public void trace_request_should_be_refused(@ArquillianResource URL baseURL) throws Exception {
        given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "TRACE")
                .expect()
                .statusCode(405)
                .when()
                .options(baseURL.toString() + "cors");
    }
}
