/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.jersey2;

import static io.restassured.RestAssured.given;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.testing.LaunchWith;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.seedstack.seed.undertow.internal.UndertowLauncher;

@RunWith(SeedITRunner.class)
@LaunchWith(UndertowLauncher.class)
public class CorsRestIT {
    private static final String WWW_OTHER_COM = "www.other.com";
    @Configuration("web.runtime.baseUrl")
    private String baseUrl;

    @Test
    public void cors_is_applied_to_rest_get() throws Exception {
        given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "GET")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseUrl + "cors");
    }

    @Test
    public void cors_is_applied_to_rest_post() throws Exception {
        given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "POST")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseUrl + "cors");
    }

    @Test
    public void cors_is_applied_to_rest_put() throws Exception {
        given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "PUT")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseUrl + "cors");
    }

    @Test
    public void cors_is_applied_to_rest_delete() throws Exception {
        given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "DELETE")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseUrl + "cors");
    }

    @Test
    public void cors_is_applied_to_rest_head() throws Exception {
        given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "HEAD")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseUrl + "cors");
    }

    @Test
    public void cors_is_applied_to_rest_options() throws Exception {
        given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "OPTIONS")
                .expect()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", WWW_OTHER_COM)
                .when()
                .options(baseUrl + "cors");
    }

    @Test
    public void trace_request_should_be_refused() throws Exception {
        given().header("Origin", WWW_OTHER_COM)
                .header("Access-Control-Request-Method", "TRACE")
                .expect()
                .statusCode(405)
                .when()
                .options(baseUrl + "cors");
    }
}