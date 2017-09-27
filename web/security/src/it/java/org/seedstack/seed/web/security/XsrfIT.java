/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.security;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;

import com.jayway.restassured.response.Response;
import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedWebIT;

public class XsrfIT extends AbstractSeedWebIT {
    public static final String XSRF_COOKIE_NAME = "XSRF-TOKEN";
    public static final String XSRF_HEADER_NAME = "X-XSRF-TOKEN";
    public static final String SESSION_COOKIE_NAME = "JSESSIONID";

    @ArquillianResource
    private URL baseURL;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(WebArchive.class)
                .addAsResource("xsrf.yaml", "META-INF/configuration/xsrf.yaml");
    }

    @Test
    @RunAsClient
    public void request_without_session_should_succeed() throws Exception {
        expect()
                .statusCode(200)
                .when()
                .get(baseURL.toString() + "xsrf-protected-without-session");
    }

    @Test
    @RunAsClient
    public void request_without_token_should_fail() throws Exception {
        String sessionId = initiateSession(baseURL).getCookie("JSESSIONID");
        given()
                .cookie(SESSION_COOKIE_NAME, sessionId)
                .expect()
                .statusCode(403)
                .when()
                .get(baseURL.toString() + "xsrf-protected-with-session");
    }

    @Test
    @RunAsClient
    public void request_with_cookie_only_should_fail() throws Exception {
        Response response = initiateSession(baseURL);
        String sessionId = response.getCookie(SESSION_COOKIE_NAME);
        String token = response.getCookie(XSRF_COOKIE_NAME);
        given()
                .cookie(SESSION_COOKIE_NAME, sessionId)
                .and()
                .cookie(XSRF_COOKIE_NAME, token)
                .expect()
                .statusCode(403)
                .when()
                .get(baseURL.toString() + "xsrf-protected-with-session");
    }

    @Test
    @RunAsClient
    public void request_with_header_only_should_fail() throws Exception {
        Response response = initiateSession(baseURL);
        String sessionId = response.getCookie(SESSION_COOKIE_NAME);
        String token = response.getCookie(XSRF_COOKIE_NAME);
        given()
                .cookie(SESSION_COOKIE_NAME, sessionId)
                .and()
                .header(XSRF_HEADER_NAME, token)
                .expect()
                .statusCode(403)
                .when()
                .get(baseURL.toString() + "xsrf-protected-with-session");
    }

    @Test
    @RunAsClient
    public void request_with_cookie_and_header_should_succeed() throws Exception {
        Response response = initiateSession(baseURL);
        String sessionId = response.getCookie(SESSION_COOKIE_NAME);
        String token = response.getCookie(XSRF_COOKIE_NAME);
        given()
                .cookie(SESSION_COOKIE_NAME, sessionId)
                .and()
                .cookie(XSRF_COOKIE_NAME, token)
                .and()
                .header(XSRF_HEADER_NAME, token)
                .expect()
                .statusCode(200)
                .when()
                .get(baseURL.toString() + "xsrf-protected-with-session");
    }

    private Response initiateSession(URL baseURL) {
        return given().auth().basic("Obiwan", "yodarulez").expect().statusCode(200).when().get(
                baseURL.toString() + "xsrf-protected-with-session");
    }
}
