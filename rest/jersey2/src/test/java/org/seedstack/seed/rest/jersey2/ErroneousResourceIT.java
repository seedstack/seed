/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2;

import static io.restassured.RestAssured.expect;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.seedstack.seed.undertow.LaunchWithUndertow;

@RunWith(SeedITRunner.class)
@LaunchWithUndertow
public class ErroneousResourceIT {
    @Configuration("runtime.web.baseUrl")
    private String baseUrl;

    @Test
    public void test_map_all_exception() {
        expect().statusCode(500).given().get(baseUrl + "/error/internal");
    }

    @Test
    public void test_map_authentication_exception() {
        expect().statusCode(401).given().get(baseUrl + "/error/authentication");
    }

    @Test
    public void test_map_authorization_exception() {
        expect().statusCode(403).given().get(baseUrl + "/error/authorization");
    }

    @Test
    public void test_map_web_application_exception() {
        expect().statusCode(404).given().get(baseUrl + "/error/notFound");
    }
}
