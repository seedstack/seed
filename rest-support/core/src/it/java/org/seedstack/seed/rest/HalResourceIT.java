/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest;

import com.jayway.restassured.response.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.json.JSONException;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedWebIT;
import org.skyscreamer.jsonassert.JSONAssert;

import java.net.URL;

import static com.jayway.restassured.RestAssured.expect;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class HalResourceIT extends AbstractSeedWebIT {

    private static final String order1 = "{\"currency\":\"USD\",\"status\":\"shipped\",\"total\":10.2," +
            "\"_links\":{\"invoice\":{\"href\":\"/invoices/873\"},\"self\":{\"href\":\"/orders/1\"},\"warehouse\":{\"href\":\"/warehouse/56\"}}}";

    private static final String order2 = "{\"currency\":\"USD\",\"status\":\"shipped\",\"total\":10.2," +
            "\"_links\":{\"invoice\":{\"href\":\"/invoices/873\"},\"self\":{\"href\":\"/orders/v2/1\"},\"warehouse\":{\"href\":\"/warehouse/56\"}}}";

    private static final String orders = "{\"currentlyProcessing\":14,\"shippedToday\":20," +
            "\"_links\":{\"next\":{\"href\":\"/orders?page=2\"},\"self\":{\"href\":\"/orders\"},\"find\":{\"href\":\"/orders{?id}\",\"templated\":true}}," +
            "\"_embedded\":{\"orders\":[" +
            "{\"total\":30.0,\"currency\":\"USD\",\"status\":\"shipped\",\"_links\":{\"basket\":{\"href\":\"/baskets/98712\"},\"self\":{\"href\":\"/order/123\"},\"customer\":{\"href\":\"/customers/7809\"}}}," +
            "{\"total\":20.0,\"currency\":\"USD\",\"status\":\"processing\",\"_links\":{\"basket\":{\"href\":\"/baskets/97213\"},\"self\":{\"href\":\"/order/124\"},\"customer\":{\"href\":\"/customers/12369\"}}}]}}";

    @ArquillianResource
    private URL baseURL;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).setWebXML("WEB-INF/web.xml");
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
