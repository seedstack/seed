/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.jersey2;

import static io.restassured.RestAssured.expect;

import io.restassured.response.Response;
import javax.ws.rs.core.HttpHeaders;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.seedstack.seed.undertow.LaunchWithUndertow;
import org.skyscreamer.jsonassert.JSONAssert;

@RunWith(SeedITRunner.class)
@LaunchWithUndertow
public class JsonHomeResourceIT {
    @Configuration("runtime.web.baseUrl")
    private String baseUrl;
    @Configuration("web.runtime.contextPath")
    private String contextPath;

    @Before
    public void setUp() {
        contextPath = "/" + contextPath;
    }

    @Test
    public void exposeJsonHome() throws JSONException {
        Response response = expect().statusCode(200).given().header(HttpHeaders.ACCEPT, "application/json")
                .get(baseUrl);

        String expectedBody = "{\"resources\":{\"http://example.org/rel/order\":{\"href-template\":\"" + contextPath
                + "orders/{id}\",\"hints\":{\"allow\":[\"GET\"],"
                + "\"formats\":{\"application/hal+json\":\"\"}},\"href-vars\":{\"id\":\"id\"}},\"http://example"
                + ".org/rel/order2\":{\"href-template\":\"" + contextPath + "orders/v2/{id}\","
                + "\"hints\":{\"allow\":[\"GET\"],\"formats\":{\"application/hal+json\":\"\"}},"
                + "\"href-vars\":{\"id\":\"id\"}}}}";

        JSONAssert.assertEquals(expectedBody, response.asString(), false);
    }
}