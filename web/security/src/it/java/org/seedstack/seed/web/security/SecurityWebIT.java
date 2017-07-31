/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.security;

import com.jayway.restassured.http.ContentType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedWebIT;

import java.net.URL;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class SecurityWebIT extends AbstractSeedWebIT {
    @ArquillianResource
    private URL baseURL;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebResource("jediCouncil.html")
                .addAsWebResource("jediAcademy.html")
                .addAsWebResource("login.html")
                .addAsWebResource("success.html")
                .addAsWebResource("logout.html")
                .addAsResource("META-INF/resources/resources/image.jpg", "META-INF/resources/resources/image.jpg")
                .addAsResource("security.yaml", "META-INF/configuration/security.yaml");
    }

    @After
    public void tearDown() throws Exception {
        expect().statusCode(200).when().get(baseURL.toString() + "logout");
    }

    @Test
    @RunAsClient
    public void request_on_secured_resource_should_send_401() throws Exception {
        expect().statusCode(401).when().get(baseURL.toString() + "jediCouncil.html");
    }

    @Test
    @RunAsClient
    public void request_on_secured_resource_with_good_basicauth_should_send_200_on_authorized_resource() throws Exception {
        given().auth().basic("Obiwan", "yodarulez").expect().statusCode(200).when().get(baseURL.toString() + "jediCouncil.html");
    }

    @Test
    @RunAsClient
    public void request_on_secured_resource_with_good_basicauth_should_send_401_on_forbidden_resource() throws Exception {
        given().auth().basic("Anakin", "imsodark").expect().statusCode(401).when().get(baseURL.toString() + "jediCouncil.html");
    }

    @Test
    @RunAsClient
    public void request_on_anonymous_resource_should_send_200() throws Exception {
        expect().statusCode(200).when().get(baseURL.toString() + "resources/image.jpg");
    }

    @Test
    @RunAsClient
    public void response_should_be_a_teapot_when_requesting_url_teapot() throws Exception {
        expect().statusCode(418).when().get(baseURL.toString() + "teapot");
    }

    @Test
    @RunAsClient
    public void logout_should_redirect() throws Exception {
        assertThat(expect().statusCode(200).when().get(baseURL.toString() + "logout").body().asString()).contains("You are logged out!");
    }

    @Test
    @RunAsClient
    public void authc_should_redirect_to_login() throws Exception {
        assertThat(expect().statusCode(200).when().get(baseURL.toString() + "protected").body().asString()).contains("Please login:");
    }

    @Test
    @RunAsClient
    public void login_success_should_redirect() throws Exception {
        assertThat(given()
                .contentType(ContentType.URLENC)
                .formParam("user", "Obiwan")
                .formParam("pw", "yodarulez")
                .expect()
                .statusCode(302)
                .when()
                .post(baseURL.toString() + "login.html")
                .header("Location"))
                .endsWith("/success.html");
    }
}
