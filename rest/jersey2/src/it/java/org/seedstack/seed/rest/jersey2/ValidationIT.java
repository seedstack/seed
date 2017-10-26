/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2;

import static com.jayway.restassured.RestAssured.given;

import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedWebIT;
import org.skyscreamer.jsonassert.JSONAssert;

public class ValidationIT extends AbstractSeedWebIT {
    @ArquillianResource
    private URL baseURL;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Test
    @RunAsClient
    public void validateBody() throws Exception {
        String body = given()
                .contentType("application/json")
                .content("{}")
                .expect()
                .statusCode(400)
                .when()
                .post(baseURL.toString() + "validating/body")
                .body()
                .asString();
        JSONAssert.assertEquals("{\"errors\":[{\"location\":\"REQUEST_BODY\",\"path\":\"attr1\"," +
                "\"message\":\"someI18nKey\",\"invalidValue\":null}]}", body, false);
    }

    @Test
    @RunAsClient
    public void validateQueryParam() throws Exception {
        String body = given()
                .accept("application/json")
                .expect()
                .statusCode(400)
                .when()
                .get(baseURL.toString() + "validating/queryparam")
                .body()
                .asString();
        JSONAssert.assertEquals("{\"errors\":[{\"location\":\"QUERY_PARAMETER\",\"path\":\"param\"," +
                "\"message\":\"someI18nKey\",\"invalidValue\":null}]}", body, false);
    }

    @Test
    @RunAsClient
    public void validateResponse() throws Exception {
        String body = given()
                .accept("application/json")
                .expect()
                .statusCode(500)
                .when()
                .get(baseURL.toString() + "validating/response")
                .body()
                .asString();
        JSONAssert.assertEquals("{\"errors\":[{\"location\":\"RESPONSE_BODY\",\"path\":\"attr1\"," +
                "\"message\":\"someI18nKey\",\"invalidValue\":null}]}", body, false);
    }
}
