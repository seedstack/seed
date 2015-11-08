/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
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

import javax.ws.rs.core.HttpHeaders;
import java.net.URL;

import static com.jayway.restassured.RestAssured.expect;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class JsonHomeResourceIT extends AbstractSeedWebIT {

    @ArquillianResource
    private URL baseURL;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).setWebXML("WEB-INF/web.xml");
    }

    @RunAsClient
    @Test
    public void expose_json_home() throws JSONException {
        Response response = expect().statusCode(200).given().header(HttpHeaders.ACCEPT, "application/json")
                .get(baseURL.toString());

        String expectedBody = "{\"resources\":{\"http://example.org/rel/order\":{\"href-template\":\"/orders/{id}\",\"hints\":{\"allow\":[\"GET\"],\"formats\":{\"application/hal+json\":\"\"}},\"href-vars\":{\"id\":\"id\"}},\"http://example.org/rel/order2\":{\"href-template\":\"/orders/v2/{id}\",\"hints\":{\"allow\":[\"GET\"],\"formats\":{\"application/hal+json\":\"\"}},\"href-vars\":{\"id\":\"id\"}}}}";

        JSONAssert.assertEquals(expectedBody, response.asString(), false);
    }
}