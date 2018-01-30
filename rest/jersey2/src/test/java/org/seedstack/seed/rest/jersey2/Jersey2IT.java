/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.jersey2;

import static io.restassured.RestAssured.expect;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.response.Response;
import javax.ws.rs.core.MediaType;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.seedstack.seed.undertow.LaunchWithUndertow;
import org.skyscreamer.jsonassert.JSONAssert;

@RunWith(SeedITRunner.class)
@LaunchWithUndertow
public class Jersey2IT {
    @Configuration("web.runtime.baseUrl")
    private String baseUrl;

    @Test
    public void basicResource() throws JSONException {
        Response response = expect().statusCode(200).given().contentType(MediaType.APPLICATION_JSON).body(
                "{ \"body\": \"hello world!\", \"author\": \"test\" }").post(baseUrl + "message");
        JSONAssert.assertEquals(response.asString(), "{\"body\":\"test says: hello world!\",\"author\":\"computer\"}",
                true);
    }

    @Test
    public void basicAsyncResource() throws JSONException {
        Response response = expect().statusCode(200).given().contentType(MediaType.APPLICATION_JSON).body(
                "{ \"body\": \"hello world!\", \"author\": \"test\" }").post(baseUrl + "async");
        JSONAssert.assertEquals(response.asString(), "{\"body\":\"test says: hello world!\",\"author\":\"computer\"}",
                true);
    }

    @Test
    public void cacheIsDisabledByDefault() {
        Response response = expect().statusCode(200).when().get(baseUrl + "hello");
        assertThat(response.header("Last-Modified")).isNotEmpty();
        assertThat(response.header("Expires")).isEqualTo("-1");
        assertThat(response.header("Cache-Control")).isEqualTo("must revalidate, private");
    }

    @Test
    public void subResourceLocator() {
        String result = expect().statusCode(200).when().get(baseUrl + "locator/sub/1").asString();
        assertThat(result).isEqualTo("sub:1");
    }

    @Test
    public void multipart() {
        expect().statusCode(200).given()
                .multiPart("file.txt", "Hello world!".getBytes())
                .post(baseUrl + "multipart");
    }
}
