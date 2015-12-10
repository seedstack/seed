/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedWebIT;
import org.skyscreamer.jsonassert.JSONAssert;

import java.net.URL;

import static com.jayway.restassured.RestAssured.expect;
import static org.assertj.core.api.Assertions.assertThat;

public class RestIT extends AbstractSeedWebIT {

    @ArquillianResource
    URL baseURL;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addAsWebResource("index.html", "index.html").setWebXML("WEB-INF/web.xml");
    }

    @RunAsClient
    @Test
    public void rest_resources_are_working() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("summary", "The world's highest resolution notebook");
        obj.put("categoryId", 1);
        obj.put("designation", "macbook pro");
        obj.put("picture", "mypictureurl");
        obj.put("price", 200.0);

        //assert response code
        String response = expect().statusCode(201).given().
                header("Accept", "application/json").header("Content-Type", "application/json").
                body(obj.toString()).post(baseURL.toString() + "products/").asString();

        // assert body
        JSONAssert.assertEquals(obj, new JSONObject(response), false);
    }

    @RunAsClient
    @Test
    public void wadl_is_disabled_by_default() {
        expect().statusCode(404).given().get(baseURL.toString() + "application.wadl");
        expect().statusCode(204).given().options(baseURL.toString() + "products/");
    }

    @RunAsClient
    @Test
    public void root_resources_are_served() {
        String response1 = expect().statusCode(200).given()
                .header("Accept", "text/plain")
                .get(baseURL.toString()).asString();

        assertThat(response1).contains("Hello World!");

        String response2 = expect().statusCode(200).given()
                .header("Accept", "application/json")
                .get(baseURL.toString()).asString();

        assertThat(response2).contains("{\"resources\":");
    }

    @RunAsClient
    @Test
    public void root_resources_let_through_when_not_matching() {
        String response = expect().statusCode(200).given()
                .header("Accept", "text/html")
                .get(baseURL.toString()).asString();

        assertThat(response).contains("<h1>Index!</h1>");
    }


    @RunAsClient
    @Test
    public void subresources_locators_are_working() {
        String result = expect().statusCode(200).when().get(baseURL.toString() + "locator/sub/1").asString();
        assertThat(result).isEqualTo("sub:1");
    }

}