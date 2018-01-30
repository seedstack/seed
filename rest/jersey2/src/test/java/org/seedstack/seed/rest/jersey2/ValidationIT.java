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
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.seedstack.seed.undertow.LaunchWithUndertow;
import org.skyscreamer.jsonassert.JSONAssert;

@RunWith(SeedITRunner.class)
@LaunchWithUndertow
public class ValidationIT {
    @Configuration("web.runtime.baseUrl")
    private String baseUrl;

    @Test
    public void validateBody() throws Exception {
        String body = given()
                .contentType("application/json")
                .body("{}")
                .expect()
                .statusCode(400)
                .when()
                .post(baseUrl + "validating/body")
                .body()
                .asString();
        JSONAssert.assertEquals("{\"errors\":[{\"location\":\"REQUEST_BODY\",\"path\":\"attr1\"," +
                "\"message\":\"someI18nKey\",\"invalidValue\":null}]}", body, false);
    }

    @Test
    public void validateQueryParam() throws Exception {
        String body = given()
                .accept("application/json")
                .expect()
                .statusCode(400)
                .when()
                .get(baseUrl + "validating/queryparam")
                .body()
                .asString();
        JSONAssert.assertEquals("{\"errors\":[{\"location\":\"QUERY_PARAMETER\",\"path\":\"param\"," +
                "\"message\":\"someI18nKey\",\"invalidValue\":null}]}", body, false);
    }

    @Test
    public void validateUnknown() throws Exception {
        String body = given()
                .accept("application/json")
                .expect()
                .statusCode(400)
                .when()
                .get(baseUrl + "validating/unknown")
                .body()
                .asString();
        JSONAssert.assertEquals("{\"errors\":[{\"location\":\"UNKNOWN\",\"path\":\"arg0.attr1\"," +
                "\"message\":\"someI18nKey\",\"invalidValue\":null}]}", body, false);
    }

    @Test
    public void validateResponse() throws Exception {
        String body = given()
                .accept("application/json")
                .expect()
                .statusCode(500)
                .when()
                .get(baseUrl + "validating/response")
                .body()
                .asString();
        JSONAssert.assertEquals("{\"errors\":[{\"location\":\"RESPONSE_BODY\",\"path\":\"attr1\"," +
                "\"message\":\"someI18nKey\",\"invalidValue\":null}]}", body, false);
    }
}
