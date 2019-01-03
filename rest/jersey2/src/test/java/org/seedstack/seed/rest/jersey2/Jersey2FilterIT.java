/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.jersey2;

import io.restassured.response.Response;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.seedstack.seed.undertow.LaunchWithUndertow;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.ws.rs.core.MediaType;

import static io.restassured.RestAssured.expect;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SeedITRunner.class)
@LaunchWithUndertow
public class Jersey2FilterIT {
    @Configuration("runtime.web.baseUrl")
    private String baseUrl;

    @Test
    public void basicResource() throws JSONException {
        Response response = expect().statusCode(200).given().contentType(MediaType.APPLICATION_JSON).body(
                "{ \"body\": \"hello world!\", \"author\": \"test\" }").post(baseUrl + "/filter/message");
        JSONAssert.assertEquals("{\"body\":\"test says: hello world!\",\"author\":\"DefaultPriorityWebFilter\"}", response.asString(),
                true);
    }
}
