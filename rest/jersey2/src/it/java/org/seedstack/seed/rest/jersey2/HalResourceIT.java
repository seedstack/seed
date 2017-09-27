/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.jersey2;

import static com.jayway.restassured.RestAssured.expect;

import com.jayway.restassured.response.Response;
import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedWebIT;
import org.skyscreamer.jsonassert.JSONAssert;

public class HalResourceIT extends AbstractSeedWebIT {
    private String order1;
    private String order2;
    private String orders;

    @ArquillianResource
    private URL baseURL;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Before
    public void setUp() throws Exception {
        order1 = "{\"currency\":\"USD\",\"status\":\"shipped\",\"total\":10.2," +
                "\"_links\":{\"invoice\":{\"href\":\"" + baseURL.getPath() + "invoices/873\"},\"self\":{\"href\":\""
                + baseURL.getPath() + "orders/1\"},\"warehouse\":{\"href\":\"" + baseURL.getPath() +
                "warehouses/56\"}}}";

        order2 = "{\"currency\":\"USD\",\"status\":\"shipped\",\"total\":10.2," +
                "\"_links\":{\"invoice\":{\"href\":\"" + baseURL.getPath() + "invoices/873\"},\"self\":{\"href\":\""
                + baseURL.getPath() + "orders/v2/1\"},\"warehouse\":{\"href\":\"" + baseURL.getPath() +
                "warehouses/56\"}}}";

        orders = "{\"currentlyProcessing\":14,\"shippedToday\":20," +
                "\"_links\":{\"next\":{\"href\":\"" + baseURL.getPath() + "orders?page=2\"},\"self\":{\"href\":\"" +
                baseURL.getPath() + "orders\"},\"find\":{\"href\":\"" + baseURL.getPath() + "orders/{id}\","
                + "\"templated\":true}}," +
                "\"_embedded\":{\"orders\":[" +
                "{\"total\":30.0,\"currency\":\"USD\",\"status\":\"shipped\",\"_links\":{\"basket\":{\"href\":\"" +
                baseURL.getPath() + "baskets/98712\"},\"self\":{\"href\":\"" + baseURL.getPath() + "orders/123\"},"
                + "\"customer\":{\"href\":\"" + baseURL.getPath() + "customers/7809\"}}}," +
                "{\"total\":20.0,\"currency\":\"USD\",\"status\":\"processing\",\"_links\":{\"basket\":{\"href\":\""
                + baseURL.getPath() + "baskets/97213\"},\"self\":{\"href\":\"" + baseURL.getPath() + "orders/124\"},"
                + "\"customer\":{\"href\":\"" + baseURL.getPath() + "customers/12369\"}}}]}}";
    }

    @RunAsClient
    @Test
    public void hal_builder() throws JSONException {
        Response response = expect().statusCode(200).given().header("Content-Type", "application/hal+json")
                .get(baseURL.toString() + "orders");

        JSONAssert.assertEquals(orders, response.asString(), true);
    }

    @RunAsClient
    @Test
    public void hal_representation() throws JSONException {
        Response response = expect().statusCode(200).given().header("Content-Type", "application/hal+json")
                .get(baseURL.toString() + "orders/1");

        JSONAssert.assertEquals(order1, response.asString(), true);
    }

    @RunAsClient
    @Test
    public void hal_representation2() throws JSONException {
        Response response = expect().statusCode(200).given().header("Content-Type", "application/hal+json")
                .get(baseURL.toString() + "orders/v2/1");

        JSONAssert.assertEquals(order2, response.asString(), true);
    }
}
