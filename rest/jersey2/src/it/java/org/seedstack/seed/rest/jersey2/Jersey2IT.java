/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2;

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

import javax.ws.rs.core.MediaType;
import java.net.URL;

import static com.jayway.restassured.RestAssured.expect;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class Jersey2IT extends AbstractSeedWebIT {

    @ArquillianResource
    private URL baseURL;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @RunAsClient
    @Test
    public void test_greetings() throws JSONException {
        Response response = expect().statusCode(200).given().contentType(MediaType.APPLICATION_JSON).body("{ \"body\": \"hello world!\", \"author\": \"test\" }").post(baseURL.toString() + "message");
        JSONAssert.assertEquals(response.asString(), "{\"body\":\"test says: hello world!\",\"author\":\"computer\"}", true);
    }

    @RunAsClient
    @Test
    public void subresources_locators_are_working() {
        String result = expect().statusCode(200).when().get(baseURL.toString() + "locator/sub/1").asString();
        assertThat(result).isEqualTo("sub:1");
    }

    @RunAsClient
    @Test
    public void test_multipart() throws JSONException {
        expect().statusCode(200).given()
                .multiPart("file.txt", "Hello world!".getBytes())
                .post(baseURL.toString() + "multipart");
    }
}
