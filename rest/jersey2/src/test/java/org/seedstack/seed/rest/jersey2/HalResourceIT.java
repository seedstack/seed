/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2;

import static io.restassured.RestAssured.expect;

import io.restassured.response.Response;
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
public class HalResourceIT {
    @Configuration("runtime.web.baseUrl")
    private String baseUrl;
    @Configuration("${web.runtime.contextPath}")
    private String contextPath;

    private String order1;
    private String order2;
    private String orders;

    @Before
    public void setUp() {
        contextPath = "/" + contextPath;
        order1 = "{\"currency\":\"USD\",\"status\":\"shipped\",\"total\":10.2," +
                "\"_links\":{\"invoice\":{\"href\":\"" + contextPath + "invoices/873\"},\"self\":{\"href\":\""
                + contextPath + "orders/1\"},\"warehouse\":{\"href\":\"" + contextPath +
                "warehouses/56\"}}}";

        order2 = "{\"currency\":\"USD\",\"status\":\"shipped\",\"total\":10.2," +
                "\"_links\":{\"invoice\":{\"href\":\"" + contextPath + "invoices/873\"},\"self\":{\"href\":\""
                + contextPath + "orders/v2/1\"},\"warehouse\":{\"href\":\"" + contextPath +
                "warehouses/56\"}}}";

        orders = "{\"currentlyProcessing\":14,\"shippedToday\":20," +
                "\"_links\":{\"next\":{\"href\":\"" + contextPath + "orders?page=2\"},\"self\":{\"href\":\"" +
                contextPath + "orders\"},\"find\":{\"href\":\"" + contextPath + "orders/{id}\","
                + "\"templated\":true}}," +
                "\"_embedded\":{\"orders\":[" +
                "{\"total\":30.0,\"currency\":\"USD\",\"status\":\"shipped\",\"_links\":{\"basket\":{\"href\":\"" +
                contextPath + "baskets/98712\"},\"self\":{\"href\":\"" + contextPath + "orders/123\"},"
                + "\"customer\":{\"href\":\"" + contextPath + "customers/7809\"}}}," +
                "{\"total\":20.0,\"currency\":\"USD\",\"status\":\"processing\",\"_links\":{\"basket\":{\"href\":\""
                + contextPath + "baskets/97213\"},\"self\":{\"href\":\"" + contextPath + "orders/124\"},"
                + "\"customer\":{\"href\":\"" + contextPath + "customers/12369\"}}}]}}";
    }

    @Test
    public void hal_builder() throws JSONException {
        Response response = expect().statusCode(200).given().header("Content-Type", "application/hal+json")
                .get(baseUrl + "/orders");

        JSONAssert.assertEquals(orders, response.asString(), true);
    }

    @Test
    public void hal_representation() throws JSONException {
        Response response = expect().statusCode(200).given().header("Content-Type", "application/hal+json")
                .get(baseUrl + "/orders/1");

        JSONAssert.assertEquals(order1, response.asString(), true);
    }

    @Test
    public void hal_representation2() throws JSONException {
        Response response = expect().statusCode(200).given().header("Content-Type", "application/hal+json")
                .get(baseUrl + "/orders/v2/1");

        JSONAssert.assertEquals(order2, response.asString(), true);
    }
}
